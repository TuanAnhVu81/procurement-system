package com.anhvt.epms.procurement.odata.processor;

/**
 * Package for OData processors
 * 
 * OData processors handle the processing of OData requests:
 * - EntityProcessor: Handles single entity operations (GET, POST, PUT, DELETE)
 * - EntityCollectionProcessor: Handles collection operations (GET list with $filter, $orderby, etc.)
 * - PrimitiveProcessor: Handles primitive property operations
 * - ComplexProcessor: Handles complex type operations
 * 
 * These processors work with Apache Olingo library to provide OData v4 support.
 * 
 * Example implementation:
 * 
 * @Component
 * public class VendorEntityProcessor implements EntityProcessor {
 *     @Override
 *     public void readEntity(ODataRequest request, ODataResponse response, 
 *                           UriInfo uriInfo, ContentType responseFormat) {
 *         // Implementation for reading single vendor
 *     }
 * }
 * 
 * Benefits of OData:
 * - Standardized query language ($filter, $select, $expand, $orderby)
 * - Compatible with SAP systems
 * - RESTful API with powerful querying capabilities
 */
public class ODataProcessorPackageInfo {
    // This is a package-info placeholder
    // Actual processor implementations will be added here
}
