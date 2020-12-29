package com.waa.workers;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Breakdown {
  String userID;
  String projectTitle;
  String productionCompany;
  String projectType;
  String startDate;
  String endDate;
  String unionStatus;
  String submissionDeadline;
  String remoteopportunity;
  String gender;
  String ageRange;
  String ethnicities;
}
