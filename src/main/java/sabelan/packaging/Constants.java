/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sabelan.packaging;

import java.io.Serializable;
import java.text.SimpleDateFormat;
/**
 *
 * @author sergey
 */
public interface Constants extends Serializable {
    
    public final static String PROGRAM_NAME = "Фасовка";
    public final static String PROP_USERNAME = "username";
    public final static String PROP_USERID = "id";
    public static final SimpleDateFormat yyyyMMdd_HHMMSS = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
}
