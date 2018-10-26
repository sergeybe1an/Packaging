/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sabelan.packaging;

import java.io.Serializable;
import com.vaadin.ui.Notification;
import sabelan.packaging.objects.PackagingTask;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.naming.NamingException;
import java.util.ArrayList;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;


/**
 *
 * @author sergey
 */
public class DbOperations implements Serializable {
    
    private final static int userAdminId = 100;
    
    public static interface ResultSetToList extends Serializable {

        Object fromResultSet(ResultSet rs) throws SQLException;
    }
    
    private DbOperations() {
    }

    public static void updTaskPackaging(int taskPackagingId) {
        final String call = "{call wms_app.PROC_UPD_TASK_PACKAGING(?)}";
        try (Connection cn = getConnection(); CallableStatement cs = cn.prepareCall(call)) {
            cs.setInt(1, taskPackagingId);
            
            cs.execute();
            cn.commit();
        } catch (Exception ex) {
            Notification.show("Проблемы с процедурой wms_app.PROC_UPD_TASK_PACKAGING(?)", Notification.Type.ERROR_MESSAGE);
            sendErrorToBD(ex, "PROC_UPD_TASK_PACKAGING");
        }
    }
    
    public static ArrayList<PackagingTask> selectPackagingTask(int taskPackagingId) {
        return selectSmthIntoList("selectPackagingTask",
            "    select vs.sku_name, tp.id_sku, e.emp_name, tp.qty_packaging, tp.checked\n" +
            "    from wms_app.task_packaging tp\n" +
            "    join wms_app.v_sku vs on vs.sku_id=tp.id_sku\n" +
            "    join factory_hope.emp e on e.id_emp=tp.packer\n" +
            "    where tp.id_packaging=" + taskPackagingId,
                rs -> new PackagingTask(taskPackagingId, rs.getInt("id_sku"), rs.getString("sku_name"), 
                                        rs.getString("emp_name"), rs.getInt("qty_packaging"), rs.getString("checked"))
        );
    }
    
    /**
     * Отправка сообщения об ошибке в БД
     *
     * @param message_param сообщение об ошибке
     * @param advanced_message дополнительная информация
     */
    public static void sendErrorToBD(Exception message_param, String advanced_message) {
        sendErrorToBD(userAdminId, PackagingUI.app, message_param, advanced_message, 0);
    }

    private static void sendErrorToBD(Integer emp_param, String app_param, Exception message_param, String advanced_message, int counter) {
        String message = null;
        try (Connection cn = getConnection();
                PreparedStatement prst = (PreparedStatement) cn.prepareStatement("INSERT INTO WMS_APP.TBERROR_LOG (EMP_ID, APP, MESSAGE) VALUES (:EMP_ID, :APP, :MESSAGE)")) {

            if (advanced_message != null && !advanced_message.isEmpty()) {
                message = "Доп.ИНф. " + advanced_message + "\n";
            }
            if (message_param instanceof SQLException) {
                SQLException ex = (SQLException) message_param;
                message += "Сообщение: " + getMessageOutException(ex) + "\n SQLState: " + ex.getSQLState() + "\n ErrorCode: " + ex.getErrorCode();
            } else {
                message += "Сообщение: " + getMessageOutException(message_param);
            }
            for (StackTraceElement element : message_param.getStackTrace()) {
                message += "\n " + element.toString();
            }
            //Ограничение в 4000 символов для базы данных
            if (message != null && message.length() > 4000) {
                message = message.substring(0, 3999);
            }
            prst.setInt(1, emp_param);
            prst.setString(2, app_param);
            prst.setString(3, message);
            prst.executeQuery();
            cn.commit();
        } catch (NamingException | SQLException ex) {
            if (counter == 0) {
                counter++;
                sendErrorToBD(emp_param != null && emp_param > 0 ? emp_param : -1, app_param, ex, "", counter);
            }
            Notification.show("Ошибка" + getMessageOutException(ex), Notification.Type.ERROR_MESSAGE);
        }
    }
    
    /**
     * Функция для упрощения однотипных запросов, которые выбирают из базы
     * список строк и превращают каждую строку в экземпляр класса.
     *
     * @param name имя запроса, для идентификации в логе ошибок.
     * @param sql сам SQL-запрос.
     * @param rstl интерфейсный метод, обрабатывающий один вызов метода next
     * класса {@link java.sql.ResultSet}
     * @return список типа {@link java.util.ArrayList}, содержащий все
     * результирующие объекты.
     */
    public static ArrayList selectSmthIntoList(String name, String sql, ResultSetToList rstl) {
        final ArrayList result = new ArrayList();
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(rstl.fromResultSet(rs));
            }
        } catch (Exception ex) {
            sendErrorToBD(ex, "selectSmthIntoList: " + name);
        }
        return result;
    }
    
    /**
     * Возвращает сообщение об ошибке с проеркой на null
     *
     * @param ex Исключение полученное в результате ошибки
     * @return возращает сообщение об ошибки с проверкой на null и Empty
     */
    public static String getMessageOutException(Exception ex) {
        StringBuilder sb = new StringBuilder();
        if (ex != null) {
            sb.append("Ошибка:");
            if (ex.getMessage() != null && !ex.getMessage().isEmpty()) {
                sb.append(ex.getMessage());
            } else if (!ex.toString().isEmpty()) {
                sb.append(ex.toString());
            } else {
                sb.append("Неизвестная ошибка");
            }
        }
        return sb.toString();
    }
    
    /**
     * Возвращает локальное соединение
     *
     * @return
     * @throws NamingException
     * @throws SQLException
     */
    public static Connection getConnection() throws NamingException, SQLException {
        Context initCtx = new InitialContext();
        Connection cn;
        switch (PackagingUI.DBID) {
            case 1:
                cn = ((DataSource) initCtx.lookup("java:comp/env/jdbc/****")).getConnection();
                break;
            case 2:
                cn = ((DataSource) initCtx.lookup("java:comp/env/jdbc/****Orel")).getConnection();
                break;
            case 3:
                cn = ((DataSource) initCtx.lookup("java:comp/env/jdbc/****Krasnodar")).getConnection();
                break;
            case 4:
                cn = ((DataSource) initCtx.lookup("java:comp/env/jdbc/****t")).getConnection();
                break;
            default:
                throw new AssertionError();
        }
        cn.setAutoCommit(false);
        return cn;
    }
}
