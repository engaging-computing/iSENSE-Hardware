/**
 * Copyright (c) 2008, iSENSE Project. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. Redistributions in binary
 * form must reproduce the above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or other materials
 * provided with the distribution. Neither the name of the University of
 * Massachusetts Lowell nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific
 * prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 */

package com.pinpoint.exceptions;

/**
 * Custom exception used to bubble up errors to the whatever
 * the UI is.
 * 
 * @deprecated Not sure whether or not this should be called. SVN is screwey and I wrote it a long time ago. 
 * @author James Dalphond <jdalphon@cs.uml.edu>
 */
public class BootloaderException extends Exception{

    public int error;
    public static int NO_COLON = 2;
    public static int INVALID_CHECKSUM = 3;
    public static int BYTE_COUNT_ERROR = 4;

    public BootloaderException(int x){
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
