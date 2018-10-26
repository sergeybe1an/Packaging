/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sabelan.packaging;

import com.vaadin.annotations.PreserveOnRefresh;
import javax.servlet.annotation.WebServlet;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.CustomizedSystemMessages;
import com.vaadin.server.Responsive;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinServlet;
import com.vaadin.server.WebBrowser;
import com.vaadin.shared.ui.ContentMode;
import sabelan.packaging.objects.PackagingTask;
import javax.servlet.annotation.WebInitParam;
import com.vaadin.ui.UI;
import com.vaadin.ui.ReconnectDialogConfiguration;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import static sabelan.packaging.DbOperations.*;

/**
 *
 * @author sergey
 */
@Theme("mytheme")
@SuppressWarnings("serial")
@PreserveOnRefresh
public class PackagingUI extends UI implements Constants {

    public final static String app = "Packaging";
    private final VerticalLayout root = new VerticalLayout();
    private final TextArea mainInformationWindow = new TextArea();
    private final TextField inputTF = new TextField();
    public static int DBID;

    public enum DB_CONNECT {
        TAMBOV, OREL, KRASNODAR, TEST
    }
    private DB_CONNECT currentDBLink;

    @Override
    protected void init(VaadinRequest request) {
        Responsive.makeResponsive(this);
        getPage().setTitle(PROGRAM_NAME);

        final ReconnectDialogConfiguration rdc = getReconnectDialogConfiguration();
        rdc.setDialogModal(true);
        rdc.setReconnectAttempts(10);
        rdc.setReconnectInterval(5000);
        rdc.setDialogText("Потеряна связь с сервером, попытка соединения...");
        rdc.setDialogTextGaveUp("Соединение с сервером не удалось. Сообщите о проблеме в отдел АСУ.");

        final WebBrowser webBrowser = Page.getCurrent().getWebBrowser();
        final String ip = webBrowser.getAddress();
        final int w = getPage().getWebBrowser().getScreenWidth();
        final int h = getPage().getWebBrowser().getScreenHeight();

        VaadinService.getCurrentRequest().getWrappedSession().setAttribute("ip", ip);
        VaadinService.getCurrentRequest().getWrappedSession().setAttribute("screen", w + "x" + h);

        try {
            determineServer();
        } catch (SocketException ex) {
            sendErrorToBD(ex, "");
            Notification.show("Ошибка" + getMessageOutException(ex), Notification.Type.ERROR_MESSAGE);
        }
        createMainWindow();
    }

    private void determineServer() throws SocketException {
        String ip;
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface iface = interfaces.nextElement();
            // filters out 127.0.0.1 and inactive interfaces
            if (iface.isLoopback() || !iface.isUp()) {
                continue;
            }
            Enumeration<InetAddress> addresses = iface.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress addr = addresses.nextElement();
                ip = addr.getHostAddress();
                if (ip.startsWith("192.168.***")) {
                    DBID = 1;
                    break;
                } else if (ip.startsWith("192.168.***")) {
                    DBID = 2;
                    break;
                } else if (ip.startsWith("192.168.***")) {
                    DBID = 3;
                    break;
                }
            }
            if (DBID != 0) {
                break;
            }
        }
        if (AppVersion.DEBUG) {
            DBID = 4;
        }

        switch (DBID) {
            case 2:
                currentDBLink = DB_CONNECT.OREL;
                break;
            case 3:
                currentDBLink = DB_CONNECT.KRASNODAR;
                break;
            case 4:
                currentDBLink = DB_CONNECT.TEST;
                break;
            default:
                currentDBLink = DB_CONNECT.TAMBOV;
                break;
        }
    }

    @WebServlet(
            value = "/*",
            asyncSupported = true,
            initParams = {
                @WebInitParam(name = "disable-xsrf-protection", value = "true")
                ,
            @WebInitParam(name = "PackagingUIServlet", value = "false")
            }
    )
    @VaadinServletConfiguration(
            ui = PackagingUI.class,
            productionMode = false
    )
    public static class DriverTimeUIServlet extends VaadinServlet {

        private static final long serialVersionUID = 180202L;

        @Override
        public void servletInitialized() {
            getService().setSystemMessagesProvider(e -> {
                final CustomizedSystemMessages messages = new CustomizedSystemMessages();
                messages.setSessionExpiredCaption("Ошибка соединения");
                messages.setSessionExpiredMessage("Откройте программу заново или обновите страницу (F5)");
                messages.setSessionExpiredNotificationEnabled(true);
                messages.setSessionExpiredURL("");
                messages.setCommunicationErrorCaption("Сервер не отвечает");
                messages.setCommunicationErrorMessage("Подождите 5-10 минут и "
                        + "попробуйте снова.\nЕсли проблема останется, "
                        + "сообщите в отдел АСУ");
                messages.setAuthenticationErrorCaption("Неизвестная ошибка");
                messages.setAuthenticationErrorMessage("Подождите 5-10 минут и "
                        + "попробуйте снова.\nЕсли проблема останется, "
                        + "сообщите в отдел АСУ");
                messages.setAuthenticationErrorNotificationEnabled(true);
                messages.setInternalErrorCaption("Внутренняя ошибка");
                messages.setInternalErrorMessage("Подождите 5-10 минут и "
                        + "попробуйте снова.\nЕсли проблема останется, "
                        + "сообщите в отдел АСУ");
                messages.setInternalErrorNotificationEnabled(true);
                return messages;
            });
        }
    }

    public void createMainWindow() {
        root.setSpacing(true);

        Label infoLabel = new Label("Packaging-" + currentDBLink.toString()
                + "<br/>Расфасуйте sku в указанной на этикетке размерности, затем прикрепите ее и отсканируйте штрих-код<br/>"
                + "<b>все этикетки именные и уже к вам прикреплены(на них присутствует ваше ФИО), поэтому сканируйте только свои</b>",
                ContentMode.HTML);

        mainInformationWindow.setWidth("100%");
        mainInformationWindow.setHeight("450px");
        mainInformationWindow.setEnabled(false);

        inputTF.setWidth("25%");
        inputTF.focus();
        inputTF.addBlurListener(e -> {
            inputTF.focus();
        });
        inputTF.addValueChangeListener(e -> inputBarcode());

        root.addComponents(infoLabel, mainInformationWindow, inputTF);

        setContent(root);
    }

    private void inputBarcode() {
        if (!inputTF.getValue().equals("")) {
            if (checkBarcodeVal(inputTF.getValue())) {

                String[] tasks = inputTF.getValue().split("TP");
                String[] withoutNullElement = Arrays.copyOfRange(tasks, 1, tasks.length);
                for (String task : withoutNullElement) {
                    ArrayList<PackagingTask> ptList = DbOperations.selectPackagingTask(Integer.parseInt(task));
                    if (!ptList.isEmpty()) {
                        PackagingTask pt = ptList.get(0);
                        mainInformationWindow.setValue(mainInformationWindow.getValue() + "\n"
                                + yyyyMMdd_HHMMSS.format(new java.util.Date()) + ", №"
                                + pt.getTaskId() + ", фасовщик:" + pt.getPackerName() + ", sku:"
                                + pt.getSkuId() + ", " + pt.getSkuName() + ", кол-во:" + pt.getPackQty());

                        if (pt.getCheckedDate() == null) {
                            DbOperations.updTaskPackaging(pt.getTaskId());
                        }
                    } else {
                        mainInformationWindow.setValue(mainInformationWindow.getValue() + "\nТакого задания не существует: " + task);
                    }
                }
            } else {
                mainInformationWindow.setValue(mainInformationWindow.getValue() + "\nНекорректное значение для штрихкода: " + inputTF.getValue());
            }

            inputTF.setValue("");
        }
    }

    private boolean checkBarcodeVal(String barCode) {
        String regExp = "^(TP\\d{1,10}){1,3}$";

        if (barCode.length() > 40) {
            return false;
        }

        Pattern pattern = Pattern.compile(regExp);
        Matcher matcher = pattern.matcher(barCode);

        if (matcher.find()) {
            return true;
        } else {
            return false;
        }
    }
}
