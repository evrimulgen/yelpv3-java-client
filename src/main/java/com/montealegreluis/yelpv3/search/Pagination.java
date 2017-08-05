/*
 * This source file is subject to the license that is bundled with this package in the file LICENSE.
 */
package com.montealegreluis.yelpv3.search;

public class Pagination {
    private final int pageSize;
    private final int total;
    private final int page;

    public boolean hasPages() {
        return total >= pageSize;
    }

    public boolean hasNext() {
        return page + 1 <= last();
    }

    public int next() {
        return page + 1;
    }

    public boolean hasPrevious() {
        return page > 1;
    }

    public int previous() {
        return page - 1;
    }

    public int first() {
        return 1;
    }

    public int last() {
        return (int) Math.ceil((double) total / pageSize);
    }

    static Pagination fromSearch(SearchCriteria criteria, int total) {
        return new Pagination(criteria, total);
    }

    private Pagination(SearchCriteria criteria, int total) {
        pageSize = criteria.limit();
        this.total = total;
        page = normalize((criteria.offset() / criteria.limit()) + 1);
    }

    private int normalize(int page) {
        if (page < 1) return 1;
        if (page > last()) return last();
        return page;
    }
}