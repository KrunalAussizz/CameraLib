package com.ieltstutorials.mylibrary.Gallery;


import java.util.ArrayList;

class Model_images {
    private String str_folder;
    private ArrayList<String> al_imagepath;

    String getStr_folder() {
        return str_folder;
    }

    public Model_images(String str_folder, ArrayList<String> al_imagepath) {
        this.str_folder = str_folder;
        this.al_imagepath = al_imagepath;
    }

    public Model_images() {
    }

    void setStr_folder(String str_folder) {
        this.str_folder = str_folder;
    }

    ArrayList<String> getAl_imagepath() {
        return al_imagepath;
    }

    void setAl_imagepath(ArrayList<String> al_imagepath) {
        this.al_imagepath = al_imagepath;
    }
}
