package com.datashare.backend.dto;

import java.util.List;


public class FileHistoryPageResponse {

    private final List<FileHistoryResponse> content;

    private final int page;

    private final int size;

    private final long totalElements;

    private final int totalPages;


    public FileHistoryPageResponse(
            List<FileHistoryResponse> content,
            int page,
            int size,
            long totalElements,
            int totalPages
    ) {

        this.content =
                List.copyOf(
                        content
                );

        this.page =
                page;

        this.size =
                size;

        this.totalElements =
                totalElements;

        this.totalPages =
                totalPages;
    }


    public List<FileHistoryResponse> getContent() {

        return List.copyOf(
                content
        );
    }


    public int getPage() {

        return page;
    }


    public int getSize() {

        return size;
    }


    public long getTotalElements() {

        return totalElements;
    }


    public int getTotalPages() {

        return totalPages;
    }
}