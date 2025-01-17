package com.iiq.rtbEngine.controller;

import com.iiq.rtbEngine.cache.DataCache;
import com.iiq.rtbEngine.db.DbManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class MainController {

    @Autowired
    private DataCache cache;

    @Autowired
    private DbManager dbManager;

    private static final String ATTRIBUTE_ID_VALUE = "atid";
    private static final String PROFILE_ID_VALUE = "pid";

    private enum UrlParam {
        ATTRIBUTE_ID(ATTRIBUTE_ID_VALUE),
        PROFILE_ID(PROFILE_ID_VALUE),
        ;

        private final String value;

        private UrlParam(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }


    @GetMapping("/attribute")
    public ResponseEntity<String> attributeRequest(HttpServletRequest request, HttpServletResponse response,
                                                   @RequestParam(name = ATTRIBUTE_ID_VALUE, required = true) Integer attributeId,
                                                   @RequestParam(name = PROFILE_ID_VALUE, required = true) Integer profileId) {
        boolean didProfileUpdate = dbManager.updateProfileAttribute(profileId, attributeId);

        if (didProfileUpdate) {
            return ResponseEntity.ok("Saved");
        } else {
            return ResponseEntity.status(500).body("Profile not saved");
        }

    }

    @GetMapping("/bid")
    public ResponseEntity<String> bidRequest(HttpServletRequest request, HttpServletResponse response,
                                             @RequestParam(name = PROFILE_ID_VALUE, required = false) Integer profileId) {
        

        return ResponseEntity.ok("Implement your code here .... ");

    }

    /**
     *
     *   GOOD LUCK !
     *
     */


}
