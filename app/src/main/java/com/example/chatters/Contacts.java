package com.example.chatters;

public class Contacts
{
    private String name, status, images;

    public Contacts()
    {
        //Default Constructor
    }

    public Contacts(String name, String status, String image)
    {
        this.name = name;
        this.status = status;
        this.images = image;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getImages()
    {
        return images;
    }

    public void setImages(String images)
    {
        this.images = images;
    }
}
