package com.lakecat.web.service;

import java.util.List;

public interface AccessGroupService {

    List<String> getUsersByGroupIds(String groupIds);

}
