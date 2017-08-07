package net.phroa.pm.model;

import lombok.Data;

import java.util.List;

@Data
public class DownloadConfirmation {

    public List<String> message;
    public String post;
}
