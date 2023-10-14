package com.bullseyeaem.core.cfrecs.models.adobeio.adobetargetrecs.catalogsearch;

import java.util.List;

import static com.bullseyeaem.core.cfrecs.util.CfRecsConstants.TARGET_CUSTOM_ATTR_IS_BULLSEYE;

public class TargetRecsCatalogSearchQuery {

    private final Meta meta;
    private final Query query;

    public TargetRecsCatalogSearchQuery(final int environmentId, final List<String> displayFields) {
        this(environmentId, displayFields, 1L, 100);
    }

    public TargetRecsCatalogSearchQuery(final int environmentId, final List<String> displayFields, final long start, final long limit) {
        meta = new Meta(environmentId, displayFields, start, limit);
        query = new SimpleQuery(List.of(TARGET_CUSTOM_ATTR_IS_BULLSEYE), "true", "eq");
    }

    public Meta getMeta() {
        return meta;
    }

    public Query getQuery() {
        return query;
    }

    static class Meta {
        private final int environmentId;
        private final long start;
        private final long limit;
        private final List<String> displayFields;

        public Meta(final int environmentId, final List<String> displayFields) {
            this(environmentId, displayFields, 1L, 100);
        }

        public Meta(final int environmentId, final List<String> displayFields, final long start, final long limit) {
            this.environmentId = environmentId;
            this.displayFields = (displayFields != null) ? displayFields : List.of();
            this.start = start;
            this.limit = limit;
        }

        public int getEnvironmentId() {
            return environmentId;
        }

        public long getStart() {
            return start;
        }

        public long getLimit() {
            return limit;
        }

        public List<String> getDisplayFields() {
            return displayFields;
        }

        public void addDisplayField(final String displayField) {
            displayFields.add(displayField);
        }
    }

    abstract static class Query {
        public abstract String getType();
    }

    static class SimpleQuery extends Query {
        private final static String TYPE = "simple";
        private final List<String> queryFields;
        private final String matchValue;
        private final String operator;
        private final String type = TYPE;

        public SimpleQuery(final List<String> queryFields, final String matchValue, final String operator) {
            this.queryFields = queryFields;
            this.matchValue = matchValue;
            this.operator = operator;
        }

        @Override
        public String getType() {
            return type;
        }

        public List<String> getQueryFields() {
            return queryFields;
        }

        public String getMatchValue() {
            return matchValue;
        }

        public String getOperator() {
            return operator;
        }
    }

    static class CompoundQuery extends Query {
        private final static String TYPE = "compound";
        private final String joinOperator;
        private final List<Query> filters;

        public CompoundQuery(final String joinOperator, final List<Query> filters) {
            this.joinOperator = joinOperator;
            this.filters = filters;
        }

        @Override
        public String getType() {
            return TYPE;
        }

        public String getJoinOperator() {
            return joinOperator;
        }

        public List<Query> getFilters() {
            return filters;
        }
    }
}

