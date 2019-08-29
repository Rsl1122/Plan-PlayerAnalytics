/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.db.sql.parsing;

/**
 * Duplicate String reducing utility class for SQL language Strings.
 */
public class Sql {
    public static final String INT = "integer";
    public static final String DOUBLE = "double";
    public static final String LONG = "bigint";
    public static final String BOOL = "boolean";

    public static final String SELECT = "SELECT ";
    public static final String DISTINCT = "DISTINCT ";
    public static final String FROM = " FROM ";
    public static final String DELETE_FROM = "DELETE" + FROM;
    public static final String WHERE = " WHERE ";
    public static final String GROUP_BY = " GROUP BY ";
    public static final String ORDER_BY = " ORDER BY ";
    public static final String INNER_JOIN = " JOIN ";
    public static final String LEFT_JOIN = " LEFT JOIN ";
    public static final String UNION = " UNION ";
    public static final String AND = " AND ";
    public static final String OR = " OR ";
    public static final String IS_NULL = " IS NULL";
    public static final String IS_NOT_NULL = " IS NOT NULL";

    private Sql() {
        /* Variable class */
    }

    public static String varchar(int length) {
        return "varchar(" + length + ')';
    }
}
