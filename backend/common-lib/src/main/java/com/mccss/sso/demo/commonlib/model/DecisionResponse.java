package com.mccss.sso.demo.commonlib.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DecisionResponse {

    /**
     * Indicates whether the requested action or decision is permitted.
     * This flag is typically used to convey authorization outcomes.
     * If true, the action is allowed; if false, it is denied.
     */
    public boolean allow;

    /**
     * A map representing additional obligations or constraints associated with the decision response.
     * The keys in the map are obligation categories (e.g., "roles", "permissions"), and the values
     * are lists of strings specifying the corresponding details for each category.
     *
     * These obligations provide supplemental information that can be used to enforce or complement
     * the decision outcome. For example, a "roles" obligation might indicate which roles should
     * be applied to the user, while a "permissions" obligation may specify required permissions
     * or additional constraints for the decision.
     */
    public Map<String, List<String>> obligations;

    /**
     * Specifies the time-to-live (TTL) value, in seconds, for caching decision responses.
     * This value is used as a hint for caching mechanisms, such as a Policy Enforcement Point (PEP),
     * to determine how long the decision response remains valid before requiring a re-evaluation.
     */
    public int cacheTtlSec = 120;
}
