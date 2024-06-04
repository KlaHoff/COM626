package com.example.mad3d.data.proj;

public class LonLat {
    public double lon,lat;
    
    public LonLat(double lon, double lat)
    {
        this.lon=lon;
        this.lat=lat;
    }
    
    public String toString()
    {
        return "lon= "+lon+ " lat="+lat;
    }
}    
