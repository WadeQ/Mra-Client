package com.wadektech.mraclient.models;

/**
 * Created by WadeQ on 22/10/2020.
 */
public class MraClient {
  private String firstName;
  private String lastName;
  private String phoneNumber ;
  private String imageUrl ;
  private double rating;

  public MraClient() {
  }

  public MraClient(String firstName, String lastName, String phoneNumber, String imageUrl, double rating) {
    this.firstName = firstName;
    this.lastName = lastName;
    this.phoneNumber = phoneNumber;
    this.imageUrl = imageUrl;
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

  public String getImageUrl() {
    return imageUrl;
  }

  public void setImageUrl(String imageUrl) {
    this.imageUrl = imageUrl;
  }

  public double getRating() {
    return rating;
  }

  public void setRating(double rating) {
    this.rating = rating;
  }
}
