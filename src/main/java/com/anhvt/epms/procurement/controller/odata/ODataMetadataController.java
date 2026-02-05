package com.anhvt.epms.procurement.controller.odata;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * OData Metadata Controller
 * Provides service metadata document at /odata/$metadata
 */
@RestController
@RequestMapping("/odata")
@Tag(name = "OData - Metadata", description = "OData service metadata endpoint")
@Slf4j
public class ODataMetadataController {

    /**
     * GET /odata/$metadata - Service metadata document
     * Returns EDMX (Entity Data Model XML) describing the service
     */
    @GetMapping(value = "/$metadata", produces = MediaType.APPLICATION_XML_VALUE)
    @Operation(
        summary = "Get service metadata (OData V4)",
        description = "Returns EDMX document describing entities, properties, and relationships"
    )
    public ResponseEntity<String> getMetadata() {
        log.info("OData: Metadata requested");
        
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<edmx:Edmx xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\" Version=\"4.0\">\n" +
            "    <edmx:DataServices>\n" +
            "        <Schema xmlns=\"http://docs.oasis-open.org/odata/ns/edm\" Namespace=\"com.anhvt.epms.procurement\">\n" +
            "            \n" +
            "            <!-- Vendor Entity Type -->\n" +
            "            <EntityType Name=\"Vendor\">\n" +
            "                <Key>\n" +
            "                    <PropertyRef Name=\"Id\"/>\n" +
            "                </Key>\n" +
            "                <Property Name=\"Id\" Type=\"Edm.Guid\" Nullable=\"false\"/>\n" +
            "                <Property Name=\"VendorCode\" Type=\"Edm.String\" MaxLength=\"20\" Nullable=\"false\"/>\n" +
            "                <Property Name=\"Name\" Type=\"Edm.String\" MaxLength=\"200\" Nullable=\"false\"/>\n" +
            "                <Property Name=\"Email\" Type=\"Edm.String\" MaxLength=\"100\"/>\n" +
            "                <Property Name=\"Phone\" Type=\"Edm.String\" MaxLength=\"20\"/>\n" +
            "                <Property Name=\"Address\" Type=\"Edm.String\" MaxLength=\"500\"/>\n" +
            "                <Property Name=\"Rating\" Type=\"Edm.Double\"/>\n" +
            "                <Property Name=\"RatingComment\" Type=\"Edm.String\"/>\n" +
            "                <Property Name=\"Status\" Type=\"Edm.String\" MaxLength=\"20\" Nullable=\"false\"/>\n" +
            "                <Property Name=\"TaxId\" Type=\"Edm.String\" MaxLength=\"50\"/>\n" +
            "                <Property Name=\"ContactPerson\" Type=\"Edm.String\" MaxLength=\"100\"/>\n" +
            "                <Property Name=\"PaymentTerms\" Type=\"Edm.String\" MaxLength=\"100\"/>\n" +
            "                <Property Name=\"Notes\" Type=\"Edm.String\"/>\n" +
            "                <Property Name=\"CreatedAt\" Type=\"Edm.DateTimeOffset\"/>\n" +
            "                <Property Name=\"UpdatedAt\" Type=\"Edm.DateTimeOffset\"/>\n" +
            "                <Property Name=\"CreatedBy\" Type=\"Edm.String\"/>\n" +
            "                <Property Name=\"UpdatedBy\" Type=\"Edm.String\"/>\n" +
            "            </EntityType>\n" +
            "            \n" +
            "            <!-- Material Entity Type -->\n" +
            "            <EntityType Name=\"Material\">\n" +
            "                <Key>\n" +
            "                    <PropertyRef Name=\"Id\"/>\n" +
            "                </Key>\n" +
            "                <Property Name=\"Id\" Type=\"Edm.Guid\" Nullable=\"false\"/>\n" +
            "                <Property Name=\"MaterialCode\" Type=\"Edm.String\" MaxLength=\"20\" Nullable=\"false\"/>\n" +
            "                <Property Name=\"Description\" Type=\"Edm.String\" MaxLength=\"500\"/>\n" +
            "                <Property Name=\"BasePrice\" Type=\"Edm.Decimal\" Nullable=\"false\"/>\n" +
            "                <Property Name=\"Currency\" Type=\"Edm.String\" MaxLength=\"3\" Nullable=\"false\"/>\n" +
            "                <Property Name=\"Unit\" Type=\"Edm.String\" MaxLength=\"20\" Nullable=\"false\"/>\n" +
            "                <Property Name=\"Category\" Type=\"Edm.String\" MaxLength=\"50\"/>\n" +
            "                <Property Name=\"Manufacturer\" Type=\"Edm.String\" MaxLength=\"100\"/>\n" +
            "                <Property Name=\"Specifications\" Type=\"Edm.String\"/>\n" +
            "                <Property Name=\"IsActive\" Type=\"Edm.Boolean\" Nullable=\"false\"/>\n" +
            "                <Property Name=\"CreatedAt\" Type=\"Edm.DateTimeOffset\"/>\n" +
            "                <Property Name=\"UpdatedAt\" Type=\"Edm.DateTimeOffset\"/>\n" +
            "                <Property Name=\"CreatedBy\" Type=\"Edm.String\"/>\n" +
            "                <Property Name=\"UpdatedBy\" Type=\"Edm.String\"/>\n" +
            "            </EntityType>\n" +
            "            \n" +
            "            <!-- PurchaseOrder Entity Type -->\n" +
            "            <EntityType Name=\"PurchaseOrder\">\n" +
            "                <Key>\n" +
            "                    <PropertyRef Name=\"Id\"/>\n" +
            "                </Key>\n" +
            "                <Property Name=\"Id\" Type=\"Edm.Guid\" Nullable=\"false\"/>\n" +
            "                <Property Name=\"PoNumber\" Type=\"Edm.String\" MaxLength=\"30\" Nullable=\"false\"/>\n" +
            "                <Property Name=\"VendorId\" Type=\"Edm.Guid\" Nullable=\"false\"/>\n" +
            "                <Property Name=\"VendorName\" Type=\"Edm.String\" MaxLength=\"200\"/>\n" +
            "                <Property Name=\"OrderDate\" Type=\"Edm.Date\" Nullable=\"false\"/>\n" +
            "                <Property Name=\"DeliveryDate\" Type=\"Edm.Date\"/>\n" +
            "                <Property Name=\"Status\" Type=\"Edm.String\" MaxLength=\"20\" Nullable=\"false\"/>\n" +
            "                <Property Name=\"StatusDisplay\" Type=\"Edm.String\" MaxLength=\"50\"/>\n" +
            "                <Property Name=\"TotalAmount\" Type=\"Edm.Decimal\" Nullable=\"false\"/>\n" +
            "                <Property Name=\"TaxRate\" Type=\"Edm.Decimal\"/>\n" +
            "                <Property Name=\"TaxAmount\" Type=\"Edm.Decimal\"/>\n" +
            "                <Property Name=\"GrandTotal\" Type=\"Edm.Decimal\" Nullable=\"false\"/>\n" +
            "                <Property Name=\"Currency\" Type=\"Edm.String\" MaxLength=\"3\" Nullable=\"false\"/>\n" +
            "                <Property Name=\"ItemCount\" Type=\"Edm.Int32\"/>\n" +
            "                <Property Name=\"ApproverName\" Type=\"Edm.String\" MaxLength=\"100\"/>\n" +
            "                <Property Name=\"ApprovedDate\" Type=\"Edm.Date\"/>\n" +
            "                <Property Name=\"RejectionReason\" Type=\"Edm.String\"/>\n" +
            "                <Property Name=\"Notes\" Type=\"Edm.String\"/>\n" +
            "                <Property Name=\"CreatedAt\" Type=\"Edm.DateTimeOffset\"/>\n" +
            "                <Property Name=\"CreatedBy\" Type=\"Edm.String\"/>\n" +
            "            </EntityType>\n" +
            "            \n" +
            "            <!-- Entity Container -->\n" +
            "            <EntityContainer Name=\"Container\">\n" +
            "                <EntitySet Name=\"Vendors\" EntityType=\"com.anhvt.epms.procurement.Vendor\"/>\n" +
            "                <EntitySet Name=\"Materials\" EntityType=\"com.anhvt.epms.procurement.Material\"/>\n" +
            "                <EntitySet Name=\"PurchaseOrders\" EntityType=\"com.anhvt.epms.procurement.PurchaseOrder\"/>\n" +
            "            </EntityContainer>\n" +
            "            \n" +
            "        </Schema>\n" +
            "    </edmx:DataServices>\n" +
            "</edmx:Edmx>";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_XML);
        
        return new ResponseEntity<>(xml, headers, HttpStatus.OK);
    }
    
    /**
     * GET /odata - Service document
     * Returns JSON listing available entity sets
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Get service document (OData V4)",
        description = "Returns JSON document listing available entity sets"
    )
    public ResponseEntity<String> getServiceDocument() {
        log.info("OData: Service document requested");
        
        String json = "{\n" +
            "    \"@odata.context\": \"http://localhost:8080/procurement/odata/$metadata\",\n" +
            "    \"value\": [\n" +
            "        {\n" +
            "            \"name\": \"Vendors\",\n" +
            "            \"kind\": \"EntitySet\",\n" +
            "            \"url\": \"Vendors\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"name\": \"Materials\",\n" +
            "            \"kind\": \"EntitySet\",\n" +
            "            \"url\": \"Materials\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"name\": \"PurchaseOrders\",\n" +
            "            \"kind\": \"EntitySet\",\n" +
            "            \"url\": \"PurchaseOrders\"\n" +
            "        }\n" +
            "    ]\n" +
            "}";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        return new ResponseEntity<>(json, headers, HttpStatus.OK);
    }
}
