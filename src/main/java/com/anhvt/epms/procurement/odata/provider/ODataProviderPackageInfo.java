package com.anhvt.epms.procurement.odata.provider;

/**
 * Package for OData EDM (Entity Data Model) providers
 * 
 * EDM providers define the metadata for OData services:
 * - Entity Types: Define the structure of entities (similar to JPA entities)
 * - Entity Sets: Collections of entities
 * - Properties: Fields of entities
 * - Navigation Properties: Relationships between entities
 * - Function/Action Imports: Custom operations
 * 
 * The EDM provider is the contract between the OData service and clients.
 * 
 * Example implementation:
 * 
 * @Component
 * public class ProcurementEdmProvider extends CsdlAbstractEdmProvider {
 *     
 *     @Override
 *     public CsdlEntityType getEntityType(FullQualifiedName entityTypeName) {
 *         if (entityTypeName.equals(ET_VENDOR_FQN)) {
 *             return new CsdlEntityType()
 *                 .setName(ET_VENDOR_NAME)
 *                 .setProperties(Arrays.asList(
 *                     new CsdlProperty().setName("Id").setType(EdmPrimitiveTypeKind.Int64.getFullQualifiedName()),
 *                     new CsdlProperty().setName("VendorCode").setType(EdmPrimitiveTypeKind.String.getFullQualifiedName())
 *                 ))
 *                 .setKey(Collections.singletonList(new CsdlPropertyRef().setName("Id")));
 *         }
 *         return null;
 *     }
 * }
 * 
 * This allows clients to query metadata at: http://localhost:8080/procurement/odata/$metadata
 */
public class ODataProviderPackageInfo {
    // This is a package-info placeholder
    // Actual EDM provider implementations will be added here
}
