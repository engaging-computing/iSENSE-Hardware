/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.pinpoint.exceptions;

/**
 *
 * @author jdalphon
 */
public class InvalidHexException extends Exception{

    public int error;
    public static int NO_COLON = 2;
    public static int INVALID_CHECKSUM = 3;
    public static int BYTE_COUNT_ERROR = 4;

    public InvalidHexException(int x){
        error = x;
    }

    public String GetErrorType(){
        if (error == NO_COLON){
            return "Hex line did not begin with a \':\'";
        } else if(error == INVALID_CHECKSUM) {
            return "Invalid Checksum";
        } else if(error == BYTE_COUNT_ERROR) {
            return "Incorrect number of bytes in record";
        } else {
            return "Unknown Error";
        }
    }
}
