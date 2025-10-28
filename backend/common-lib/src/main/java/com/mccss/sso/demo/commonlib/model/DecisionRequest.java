package com.mccss.sso.demo.commonlib.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DecisionRequest {
    /**
     * Represents the application context for a decision request. Valid values may include "frontoffice", "backoffice", etc.
     */
    public String app;

    /**
     * Represents the HTTP method of the request in the decision-making process, such as GET, POST, PUT, or DELETE.
     */
    public String action;

    /**
     * Represents the path or resource associated with the decision request.
     * Typically corresponds to the endpoint or target for which access is being evaluated.
     */
    public String resource;
}
