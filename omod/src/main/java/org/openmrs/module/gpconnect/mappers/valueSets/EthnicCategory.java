package org.openmrs.module.gpconnect.mappers.valueSets;

import org.hl7.fhir.dstu3.model.Coding;
import org.openmrs.module.gpconnect.util.CodeSystems;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

//value from: https://fhir.nhs.uk/STU3/ValueSet/CareConnect-EthnicCategory-1
public enum EthnicCategory {
	A("British, Mixed British"), B("Irish"), C("Any other White background"), C2("Northern Irish"), C3(
	        "Other white, white unspecified"), CA("English"), CB("Scottish"), CC("Welsh"), CD("Cornish"), CE(
	        "Cypriot (part not stated)"), CF("Greek"), CG("Greek Cypriot"), CH("Turkish"), CJ("Turkish Cypriot"), CK(
	        "Italian"), CL("Irish Traveller"), CM("Traveller"), CN("Gypsy/Romany"), CP("Polish"), CQ(
	        "All republics which made up the former USSR"), CR("Kosovan"), CS("Albanian"), CT("Bosnian"), CU("Croatian"), CV(
	        "Serbian"), CW("Other republics which made up the former Yugoslavia"), CX("Mixed white"), CY(
	        "Other white European, European unspecified, European mixed"), D("White and Black Caribbean"), E(
	        "White and Black African"), F("White and Asian"), G("Any other mixed background"), GA("Black and Asian"), GB(
	        "Black and Chinese"), GC("Black and White"), GD("Chinese and White"), GE("Asian and Chinese"), GF(
	        "Other Mixed, Mixed Unspecified"), H("Indian or British Indian"), J("Pakistani or British Pakistani"), K(
	        "Bangladeshi or British Bangladeshi"), L("Any other Asian background"), LA("Mixed Asian"), LB("Punjabi"), LC(
	        "Kashmiri"), LD("East African Asian"), LE("Sri Lanka"), LF("Tamil"), LG("Sinhalese"), LH("British Asian"), LJ(
	        "Caribbean Asian"), LK("Other Asian, Asian unspecified"), M("Caribbean"), N("African"), P(
	        "Any other Black background"), PA("Somali"), PB("Mixed Black"), PC("Nigerian"), PD("Black British"), PE(
	        "Other Black, Black unspecified"), R("Chinese"), S("Any other ethnic group"), SA("Vietnamese"), SB("Japanese"), SC(
	        "Filipino"), SD("Malaysian"), SE("Any Other Group"), Z("Not stated");
	
	private final String display;
	
	EthnicCategory(String display) {
		this.display = display;
	}
	
	public Coding getCoding() {
		return new Coding(CodeSystems.ETHNIC_CATEGORY, name(), display);
	}

    public static List<String> names() {
		return Arrays.stream(values())
				.map(Enum::name)
				.collect(Collectors.toList());
	}
}
