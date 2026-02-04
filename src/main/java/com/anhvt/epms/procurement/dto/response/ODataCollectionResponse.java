package com.anhvt.epms.procurement.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * OData V4-style collection response wrapper
 * Mimics OData JSON format for compatibility with OData clients
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ODataCollectionResponse<T> {
    
    /**
     * OData context (metadata URL)
     */
    @JsonProperty("@odata.context")
    private String context;
    
    /**
     * Total count of records (when $count=true)
     */
    @JsonProperty("@odata.count")
    private Long count;
    
    /**
     * Next page link (for server-side pagination)
     */
    @JsonProperty("@odata.nextLink")
    private String nextLink;
    
    /**
     * Collection of entities
     */
    @JsonProperty("value")
    private List<T> value;
}
