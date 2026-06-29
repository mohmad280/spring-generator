package com.mas.spring_generator.service.parser.impl;

import com.mas.spring_generator.DTO.*;
import com.mas.spring_generator.service.parser.ErdParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.tomcat.util.IntrospectionUtils.capitalize;

@Service
@RequiredArgsConstructor
public class DbmlParser implements ErdParser {

    private static final Pattern RELATION_PATTERN =
            Pattern.compile("Ref:\\s*(\\w+)\\.(\\w+)\\s*([>\\-<>]+)\\s*(\\w+)\\.(\\w+)");

    private static final Pattern FIELD_PATTERN =
            Pattern.compile("(\\w+)\\s+(\\w+)(?:\\s*\\[(.*)])?");

    @Override
    public List<EntityRequest> parse(MultipartFile file) {
        try {
            // تحويل الملف ل string
            String content = new String(file.getBytes());
            return parseTables(content);

        } catch (IOException e) {
            throw new RuntimeException("Failed to read ERD file", e);
        }
    }

    private List<EntityRequest> parseTables(String content) {

        List<EntityRequest> entities = new ArrayList<>();

        Pattern pattern = Pattern.compile("Table\\s+(\\w+)\\s*\\{([^}]*)\\}", Pattern.DOTALL);
        // بتفذ ال regex
        Matcher matcher = pattern.matcher(content);

        // todo اذا السكيرتي موجود لا تعمل انتتي ال يوزر
        // المشكله انه لما يكون عندي المستخدم باعث بده يعمل انتتي اليوزر و طالب سكيرتي بصير في ثنين انتتي User
        while (matcher.find()) {

            String tableName = matcher.group(1);
            String body = matcher.group(2);

            EntityRequest entity = new EntityRequest();
            entity.setName(capitalize(tableName));

            // العلاقات اول اشي عشان ابعثها لل field parser
            List<RelationRequest> relations = parseRelations(tableName, content);

            // يتم ارسال محتوى الجدول فقط
            List<FieldRequest> fields = parseFields(body, relations);
            entity.setFields(fields);


            entity.setRelations(relations);

            entities.add(entity);
        }

        return entities;
    }

    private List<FieldRequest> parseFields(String body,
                                           List<RelationRequest> relations) {

        List<FieldRequest> fields = new ArrayList<>();

        Matcher matcher = FIELD_PATTERN.matcher(body);

        while (matcher.find()) {

            String name = matcher.group(1);

            boolean isRelationField = relations.stream()
                    .anyMatch(r -> r.getFieldName()
                            .equals(toCamel(name)));

            if (isRelationField) {
                continue;
            }

            String type = matcher.group(2);
            String constraints = matcher.group(3);

            FieldRequest field = new FieldRequest();
            field.setName(toCamel(name));
            field.setType(mapType(type));

            if (constraints != null) {
                field.setValidations(parseValidations(constraints));
            }

            fields.add(field);
        }

        return fields;
    }

    private List<ValidationRequest> parseValidations(String constraints) {

        List<ValidationRequest> list = new ArrayList<>();

        String[] parts = constraints.split(",");

        for (String part : parts) {

            part = part.trim();

            switch (part) {

                case "not null" -> {
                    ValidationRequest v = new ValidationRequest();
                    v.setType(ValidationType.NOT_NULL);
                    list.add(v);
                }

                case "unique" -> {
                    ValidationRequest v = new ValidationRequest();
                    v.setType(ValidationType.UNIQUE);
                    list.add(v);
                }

                default -> {
                    if (part.startsWith("min:")) {
                        ValidationRequest v = new ValidationRequest();
                        v.setType(ValidationType.MIN);
                        v.setMin(Integer.parseInt(part.split(":")[1]));
                        list.add(v);
                    }

                    if (part.startsWith("max:")) {
                        ValidationRequest v = new ValidationRequest();
                        v.setType(ValidationType.MAX);
                        v.setMax(Integer.parseInt(part.split(":")[1]));
                        list.add(v);
                    }
                }
            }
        }

        return list;
    }

    private FieldType mapType(String fieldType) {

        return switch (fieldType.toLowerCase()) {
            case "varchar", "text" -> FieldType.String;
            case "int", "integer" -> FieldType.Integer;
            case "bigint" -> FieldType.Long;
            case "boolean" -> FieldType.Boolean;
            case "date" -> FieldType.LocalDate;
            case "timestamp" -> FieldType.LocalDateTime;
            default -> FieldType.String;
        };
    }

    private String toCamel(String input) {
        String[] parts = input.split("_");

        StringBuilder result = new StringBuilder(parts[0].toLowerCase());

        for (int i = 1; i < parts.length; i++) {
            result.append(parts[i].substring(0, 1).toUpperCase())
                    .append(parts[i].substring(1).toLowerCase());
        }

        return result.toString();
    }

    private List<RelationRequest> parseRelations(String currentTable ,String content) {

        List<RelationRequest> relations = new ArrayList<>();

        Matcher matcher = RELATION_PATTERN.matcher(content);

        while (matcher.find()) {

            String fromTable = matcher.group(1);
            String fromField = matcher.group(2);
            String symbol = matcher.group(3);
            String toTable = matcher.group(4);
            String toField = matcher.group(5);

            // تجاهل العلاقة إذا لم تكن تخص الجدول الحالي
            if (!fromTable.equalsIgnoreCase(currentTable)) {
                continue;
            }

            RelationRequest relation = new RelationRequest();

            // todo
            relation.setFieldName(toCamel(fromField));
            relation.setTargetEntity(capitalize(toTable));
            relation.setMappedBy(toField);
            relation.setType(mapRelationType(symbol));

            relations.add(relation);
        }

        return relations;
    }

    private RelationType mapRelationType(String symbol) {

        return switch (symbol) {
            case ">" -> RelationType.MANY_TO_ONE;
            case "-" -> RelationType.ONE_TO_ONE;
            case "<>" -> RelationType.MANY_TO_MANY;
            default -> throw new IllegalArgumentException("Unknown relation: " + symbol);
        };
    }

    private List<RelationRequest> filterRelationsForTable(
            String table,
            List<RelationRequest> all
    ) {
        return all.stream()
                .filter(r -> r.getFieldName() != null)
                .toList();
    }
}
