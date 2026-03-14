package ru.vortex.geocoder.util;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import ru.vortex.geocoder.annotation.Filterable;
import ru.vortex.geocoder.dto.SearchRequest;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GenericSpecificationBuilder {

    private static final Map<Class<?>, List<String>> ALLOWED_FIELDS_CACHE = new ConcurrentHashMap<>();

    public static <T> Specification<T> buildSpecification(SearchRequest request, Class<T> entityClass) {
        if (request == null || request.getFilters() == null || request.getFilters().isEmpty()) {
            return Specification.where(null);
        }

        List<String> allowedFields = getAllowedFields(entityClass);

        return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            for (SearchRequest.FilterCondition condition : request.getFilters()) {
                if (!allowedFields.contains(condition.getField())) {
                    throw new IllegalArgumentException("Filtering by field '" + condition.getField() + "' is not allowed");
                }

                Field field = findField(entityClass, condition.getField());
                if (field == null) continue;

                Path<?> fieldPath = root.get(condition.getField());
                Predicate predicate = null;

                switch (condition.getOperator().toUpperCase()) {
                    case "EQUAL":
                        predicate = criteriaBuilder.equal(fieldPath, condition.getValue());
                        break;
                    case "LIKE":
                        predicate = criteriaBuilder.like((Path<String>) fieldPath, "%" + condition.getValue() + "%");
                        break;
                    case "GT":
                        predicate = criteriaBuilder.greaterThan((Path<? extends Comparable>) fieldPath, (Comparable) condition.getValue());
                        break;
                    case "LT":
                        predicate = criteriaBuilder.lessThan((Path<? extends Comparable>) fieldPath, (Comparable) condition.getValue());
                        break;
                    case "IN":
                        CriteriaBuilder.In<Object> inClause = criteriaBuilder.in(fieldPath);
                        ((List<?>) condition.getValue()).forEach(inClause::value);
                        predicate = inClause;
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown operator: " + condition.getOperator());
                }

                if (predicate != null) {
                    predicates.add(predicate);
                }
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static List<String> getAllowedFields(Class<?> entityClass) {
        return ALLOWED_FIELDS_CACHE.computeIfAbsent(entityClass, cls -> {
            List<String> allowed = new ArrayList<>();
            for (Field field : cls.getDeclaredFields()) {
                if (field.isAnnotationPresent(Filterable.class)) {
                    allowed.add(field.getName());
                }
            }
            Class<?> superClass = cls.getSuperclass();
            while (superClass != null && !superClass.equals(Object.class)) {
                for (Field field : superClass.getDeclaredFields()) {
                    if (field.isAnnotationPresent(Filterable.class)) {
                        allowed.add(field.getName());
                    }
                }
                superClass = superClass.getSuperclass();
            }
            return allowed;
        });
    }

    private static Field findField(Class<?> cls, String fieldName) {
        try {
            return cls.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            Class<?> superClass = cls.getSuperclass();
            while (superClass != null) {
                try {
                    return superClass.getDeclaredField(fieldName);
                } catch (NoSuchFieldException ex) {
                    superClass = superClass.getSuperclass();
                }
            }
            return null;
        }
    }
}