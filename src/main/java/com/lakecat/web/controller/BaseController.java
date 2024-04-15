package com.lakecat.web.controller;

import java.util.function.Supplier;

import com.lakecat.web.exception.BusinessException;
import com.lakecat.web.response.Response;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public abstract class BaseController<T> {

    protected Response<T> createResponse(Supplier<Response<T>> supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Invoke source: {}, Exception: {}", Thread.currentThread().getStackTrace()[2].toString(), e.getMessage());
            return Response.fail(2, e.getMessage());
        }
    }

}