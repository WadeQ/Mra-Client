package com.wadektech.mraclient.models;

/**
 * Created by WadeQ on 23/10/2020.
 */
public class JobSeekerInfo {
  private String firstName;
  private String lastName;
  private String phoneNumber;
  private String profileImage;
  private double rating ;

  public JobSeekerInfo() {
  }

  public JobSeekerInfo(String firstName, String lastName, String phoneNumber, String profileImage, double rating) {
    this.firstName = firstName;
    this.lastName = lastName;
    this.phoneNumber = phoneNumber;
    this.profileImage = profileImage;
    this.rating = rating;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getPhoneNumber() {
    return phoneNumber;
  }

  public void setPhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }

  public String getProfileImage() {
    return profileImage;
  }

  public void setProfileImage(String profileImage) {
    this.profileImage = profileImage;
  }

  public double getRating() {
    return rating;
  }

  public void setRating(double rating) {
    this.rating = rating;
  }
}
