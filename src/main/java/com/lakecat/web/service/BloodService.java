package com.lakecat.web.service;

import com.lakecat.web.vo.blood.BloodRequest;
import com.lakecat.web.vo.blood.Response;

import java.util.List;

public interface BloodService {

    Response blood(BloodRequest bloodRequest);

    List<String> bloodOwners(BloodRequest bloodRequest);

    void wrapBloodResponse(Response response);

    void sync();
}
