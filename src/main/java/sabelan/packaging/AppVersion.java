package sabelan.packaging;

import java.io.Serializable;

/**
 * Класс для отслеживания версии программы. Используется дженкинсом для
 * проставления даты, времени и номера билда.
 *
 * @version 3
 * @author SapogaN
 */
public class AppVersion implements Serializable {

    private static final long serialVersionUID = 171207L;
//==============================================================================
//========================ЗДЕСЬ_МОЖНО_МЕНЯТЬ_ПАРАМЕТРЫ==========================
//==============================================================================
    /**
     * Номер билда на тот случай, если собирал не дженкинс.
     *
     * @since 3
     */
    private static final String OLD_BUILD_NUMBER = "develop";
    /**
     * Включен ли дебаг. Дженкинс попробует отключить его.
     *
     * @since 2
     */
    public static final boolean DEBUG = false;
    /**
     * Нужно ли будет обязательно обновиться всем пользователям до текущей
     * версии.
     *
     * @since 2
     */
    private static final boolean FORCE_UPDATE = false;
//==============================================================================
//========================ЗДЕСЬ_НЕЛЬЗЯ_МЕНЯТЬ_ПАРАМЕТРЫ=========================
//==============================================================================
    /**
     * Строка для замены дженкинсом через команду sed. Эту строку не менять!
     *
     * @since 1
     */
    private static final String BUILD_JENKINS_NUMBER = "jenkins_replace_build";
    /**
     * Строка для замены дженкинсом через команду sed. Эту строку не менять!
     *
     * @since 3
     */
    private static final String BUILD_JENKINS_DATE = "jenkins_replace_date";
    /**
     * Строка для замены дженкинсом через команду sed. Эту строку не менять!
     *
     * @since 3
     */
    private static final String BUILD_JENKINS_TIME = "jenkins_replace_time";
//==============================================================================
//=====================ИТОГОВЫЕ_КОНСТАНТЫ_ДЛЯ_ИСПОЛЬЗОВАНИЯ=====================
//==============================================================================
    /**
     * Строка с номером сборки программы.
     *
     * @since 3
     */
    public static final String BUILD_NUMBER
            = BUILD_JENKINS_NUMBER.contains("jenkins")
            ? OLD_BUILD_NUMBER
            : BUILD_JENKINS_NUMBER;
    /**
     * Строка с датой и номером сборки программы.
     *
     * @since 3
     */
    public static final String BUILD_DATE_NUMBER
            = BUILD_NUMBER.equals(OLD_BUILD_NUMBER)
            || BUILD_JENKINS_DATE.contains("jenkins")
            ? OLD_BUILD_NUMBER
            : BUILD_JENKINS_DATE + "#J" + BUILD_NUMBER;

    /**
     * Строка с датой, временем и номером сборки программы.
     *
     * @since 3
     */
    public static final String BUILD_DATE_TIME_NUMBER
            = BUILD_NUMBER.equals(OLD_BUILD_NUMBER)
            || BUILD_JENKINS_DATE.contains("jenkins")
            || BUILD_JENKINS_TIME.contains("jenkins")
            ? OLD_BUILD_NUMBER
            : BUILD_JENKINS_DATE + "_" + BUILD_JENKINS_TIME + "#J" + BUILD_NUMBER;
//==============================================================================
//====================================МЕТОДЫ====================================
//==============================================================================

    /**
     * Получение номера билда в виде числа.
     *
     * @since 2
     * @return номер билда, проставленный дженкинсом.
     */
    public static int getBuildJenkinsNumber() {
        try {
            return Integer.parseInt(BUILD_JENKINS_NUMBER);
        } catch (NumberFormatException ex) {
            return -1;
        }
    }
}
