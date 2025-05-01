package com.hotel.admin.app.dto.room;

 import lombok.AllArgsConstructor;
 import lombok.Getter;
 import lombok.Setter;
 import java.util.List;

 @Getter @Setter @AllArgsConstructor
 public class BulkRoomUploadResponse {
     private int successCount;
     private int failureCount;
     private List<String> errors; // List of error messages for failed rows
 }