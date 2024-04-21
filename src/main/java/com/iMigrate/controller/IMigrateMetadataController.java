package com.iMigrate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.iMigrate.service.IMigrateMetaDataService;

@RestController
@RequestMapping("/metadata")
public class IMigrateMetadataController {

	@Autowired
	public IMigrateMetaDataService iMigrateMetaDataService;

	@GetMapping("/tables")
	public ResponseEntity getMetaData() throws Exception {
		try {
			iMigrateMetaDataService.getDatabaseTableSchemaMetadata();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ResponseEntity<>(HttpStatus.OK);
	}


}
