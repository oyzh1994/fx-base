package cn.oyzh.fx.plus.i18n;

import lombok.experimental.UtilityClass;

/**
 * @author oyzh
 * @since 2024/5/13
 */
@UtilityClass
public class I18nHelper {

    public static final String ALREADY_EXISTS = "base.alreadyExists";

    public static final String FAIL = "base.fail";

    public static final String SUCCESS = "base.success";

    public static final String BINARY = "base.binary";

    public static final String JSON = "base.json";

    public static final String STRING = "base.string";

    public static final String HEX = "base.hex";

    public static final String RAW = "base.raw";

    public static final String TRANSPORT = "base.transport";

    public static final String NODE = "base.node";

    public static final String KEY = "base.key";

    public static final String ERROR = "base.error";

    public static final String INFO = "base.info";

    public static final String EXPORT = "base.export";

    public static final String IMPORT = "base.import";

    public static final String DATA = "base.data";

    public static final String CONTENT = "base.content";

    public static final String CAN_NOT = "base.canNot";

    public static final String IS = "base.is";

    public static final String EMPTY = "base.empty";

    public static final String OPERATION = "base.operation";

    public static final String CANCEL = "base.cancel";

    public static final String TIMOUT = "base.timout";

    public static final String EXCEPTION = "base.exception";

    public static final String ING = "base.ing";

    public static final String SEARCH = "base.search";

    public static final String NO = "base.no";

    public static final String MATCH = "base.match";

    public static final String MATCHED = "base.matched";

    public static final String DRAG_TIP1 = "base.dragTip1";

    public static final String FINISH = "base.finish";

    public static final String FILE = "base.file";

    public static final String NOT = "base.not";

    public static final String EXISTS = "base.exists";

    public static final String FOLDER = "base.folder";

    public static final String SUPPORT = "base.support";

    public static final String FORMAT = "base.format";

    public static final String INVALID = "base.invalid";

    public static final String PARSE = "base.parse";

    public static final String PROCESS = "base.process";

    public static final String FILE_NAME = "base.fileName";

    public static final String CHARSET = "base.charset";

    public static final String PLATFORM = "base.platform";

    public static final String VERSION = "base.version";

    public static final String SIZE = "base.size";

    public static final String TOTAL = "base.total";

    public static final String LINE = "base.line";

    public static final String PREFIX = "base.prefix";

    public static final String CONNECT = "base.connect";

    public static final String ALL = "base.all";

    public static final String DATABASE = "base.database";

    public static final String INIT = "base.init";

    public static final String TARGET = "base.target";

    public static final String SOURCE = "base.source";

    public static final String ONLY = "base.only";

    public static final String SINGLE = "base.single";

    public static final String CHOOSE = "base.choose";

    public static final String NEVER = "base.never";

    public static final String EXPIRE = "base.expire";

    public static final String DELETE = "base.delete";

    public static final String KEYS = "base.keys";

    public static final String DELETED = "base.deleted";

    public static final String FOUND = "base.found";

    public static final String CURRENT = "base.current";

    public static final String START = "base.start";

    public static final String COPY = "base.copy";

    public static final String TEST = "base.test";

    public static final String RESTORE = "base.restore";

    public static final String QUIT = "base.quit";

    public static final String USER_NAME = "base.userName";

    public static final String PASSWORD = "base.password";

    public static final String PERMS = "base.perms";

    public static final String NAME = "base.name";

    public static final String TRAY = "base.tray";

    public static final String VALUE = "base.value";

    public static final String COST = "base.cost";

    public static final String ENV = "base.env";

    public static final String SOFT = "base.soft";

    public static final String LATITUDE = "base.latitude";

    public static final String LONGITUDE = "base.longitude";

    public static final String HOST = "base.host";

    public static final String PORT = "base.port";

    public static final String READONLY_MODE = "base.readonlyMode";

    public static String operationFail() {
        return I18nResourceBundle.i18nString(OPERATION, FAIL);
    }

    public static String operationCancel() {
        return I18nResourceBundle.i18nString(OPERATION, CANCEL);
    }

    public static String operationSuccess() {
        return I18nResourceBundle.i18nString(OPERATION, SUCCESS);
    }

    public static String operationTimout() {
        return I18nResourceBundle.i18nString(OPERATION, TIMOUT);
    }

    public static String operationException() {
        return I18nResourceBundle.i18nString(OPERATION, EXCEPTION);
    }

    public static String operationIng() {
        return I18nResourceBundle.i18nString(OPERATION, ING);
    }

    public static String operationFinish() {
        return I18nResourceBundle.i18nString(OPERATION, FINISH);
    }

    public static String contentCanNotEmpty() {
        return I18nResourceBundle.i18nString(CONTENT, CAN_NOT, EMPTY);
    }

    public static String contentIsEmpty() {
        return I18nResourceBundle.i18nString(CONTENT, IS, EMPTY);
    }

    public static String alreadyExists() {
        return I18nResourceBundle.i18nString(ALREADY_EXISTS);
    }

    public static String fail() {
        return I18nResourceBundle.i18nString(FAIL);
    }

    public static String success() {
        return I18nResourceBundle.i18nString(SUCCESS);
    }

    public static String binary() {
        return I18nResourceBundle.i18nString(BINARY);
    }

    public static String json() {
        return I18nResourceBundle.i18nString(JSON);
    }

    public static String string() {
        return I18nResourceBundle.i18nString(STRING);
    }

    public static String hex() {
        return I18nResourceBundle.i18nString(HEX);
    }

    public static String raw() {
        return I18nResourceBundle.i18nString(RAW);
    }

    public static String searching() {
        return I18nResourceBundle.i18nString(SEARCH, ING);
    }

    public static String transport() {
        return I18nResourceBundle.i18nString(TRANSPORT);
    }

    public static String node() {
        return I18nResourceBundle.i18nString(NODE);
    }

    public static String transportNode() {
        return I18nResourceBundle.i18nString(TRANSPORT, NODE);
    }

    public static String transportKey() {
        return I18nResourceBundle.i18nString(TRANSPORT, KEY);
    }

    public static String transportIng() {
        return I18nResourceBundle.i18nString(TRANSPORT, ING);
    }

    public static String error() {
        return I18nResourceBundle.i18nString(ERROR);
    }

    public static String info() {
        return I18nResourceBundle.i18nString(INFO);
    }

    public static String errorInfo() {
        return I18nResourceBundle.i18nString(ERROR, INFO);
    }

    public static String export() {
        return I18nResourceBundle.i18nString(EXPORT);
    }

    public static String _import() {
        return I18nResourceBundle.i18nString(IMPORT);
    }

    public static String importNode() {
        return I18nResourceBundle.i18nString(IMPORT, NODE);
    }

    public static String exportNode() {
        return I18nResourceBundle.i18nString(EXPORT, NODE);
    }

    public static String importKey() {
        return I18nResourceBundle.i18nString(IMPORT, KEY);
    }

    public static String importIng() {
        return I18nResourceBundle.i18nString(IMPORT, ING);
    }

    public static String exportKey() {
        return I18nResourceBundle.i18nString(EXPORT, KEY);
    }

    public static String exportIng() {
        return I18nResourceBundle.i18nString(EXPORT, ING);
    }

    public static String data() {
        return I18nResourceBundle.i18nString(DATA);
    }

    public static String importData() {
        return I18nResourceBundle.i18nString(IMPORT, DATA);
    }

    public static String exportData() {
        return I18nResourceBundle.i18nString(EXPORT, DATA);
    }

    public static String content() {
        return I18nResourceBundle.i18nString(CONTENT);
    }

    public static String contentAlreadyExists() {
        return I18nResourceBundle.i18nString(CONTENT, ALREADY_EXISTS);
    }

    public static String cancel() {
        return I18nResourceBundle.i18nString(CANCEL);
    }

    public static String timout() {
        return I18nResourceBundle.i18nString(TIMOUT);
    }

    public static String exception() {
        return I18nResourceBundle.i18nString(EXCEPTION);
    }

    public static String search() {
        return I18nResourceBundle.i18nString(SEARCH);
    }

    public static String match() {
        return I18nResourceBundle.i18nString(MATCH);
    }

    public static String matched() {
        return I18nResourceBundle.i18nString(MATCHED);
    }

    public static String noMatchedData() {
        return I18nResourceBundle.i18nString(NO, MATCHED, DATA);
    }

    public static String noMatchedKey() {
        return I18nResourceBundle.i18nString(NO, MATCHED, KEY);
    }

    public static String dragTip1() {
        return I18nResourceBundle.i18nString(DRAG_TIP1);
    }

    public static String finish() {
        return I18nResourceBundle.i18nString(FINISH);
    }

    public static String is() {
        return I18nResourceBundle.i18nString(IS);
    }

    public static String file() {
        return I18nResourceBundle.i18nString(FILE);
    }

    public static String not() {
        return I18nResourceBundle.i18nString(NOT);
    }

    public static String exists() {
        return I18nResourceBundle.i18nString(EXISTS);
    }

    public static String fileNotExists() {
        return I18nResourceBundle.i18nString(FILE, NOT, EXISTS);
    }

    public static String folder() {
        return I18nResourceBundle.i18nString(FOLDER);
    }

    public static String notSupportFolder() {
        return I18nResourceBundle.i18nString(NOT, SUPPORT, FOLDER);
    }

    public static String format() {
        return I18nResourceBundle.i18nString(FORMAT);
    }

    public static String invalid() {
        return I18nResourceBundle.i18nString(INVALID);
    }

    public static String invalidFormat() {
        return I18nResourceBundle.i18nString(INVALID, FORMAT);
    }

    public static String parse() {
        return I18nResourceBundle.i18nString(PARSE);
    }

    public static String parseFail() {
        return I18nResourceBundle.i18nString(PARSE, FAIL);
    }

    public static String process() {
        return I18nResourceBundle.i18nString(PROCESS);
    }

    public static String processing() {
        return I18nResourceBundle.i18nString(PROCESS, ING);
    }

    public static String fileName() {
        return I18nResourceBundle.i18nString(FILE_NAME);
    }

    public static String charset() {
        return I18nResourceBundle.i18nString(CHARSET);
    }

    public static String platform() {
        return I18nResourceBundle.i18nString(PLATFORM);
    }

    public static String version() {
        return I18nResourceBundle.i18nString(VERSION);
    }

    public static String size() {
        return I18nResourceBundle.i18nString(SIZE);
    }

    public static String total() {
        return I18nResourceBundle.i18nString(TOTAL);
    }

    public static String line() {
        return I18nResourceBundle.i18nString(LINE);
    }

    public static String prefix() {
        return I18nResourceBundle.i18nString(PREFIX);
    }

    public static String importProcessing() {
        return I18nResourceBundle.i18nString(IMPORT, PROCESS, ING);
    }

    public static String exportProcessing() {
        return I18nResourceBundle.i18nString(EXPORT, PROCESS, ING);
    }

    public static String transportProcessing() {
        return I18nResourceBundle.i18nString(TRANSPORT, PROCESS, ING);
    }

    public static String transportStarting() {
        return I18nResourceBundle.i18nString(TRANSPORT, START, ING);
    }

    public static String connect() {
        return I18nResourceBundle.i18nString(CONNECT);
    }

    public static String fileProcessing() {
        return I18nResourceBundle.i18nString(FILE, PROCESS, ING);
    }

    public static String all() {
        return I18nResourceBundle.i18nString(ALL);
    }

    public static String database() {
        return I18nResourceBundle.i18nString(DATABASE);
    }

    public static String allDatabase() {
        return I18nResourceBundle.i18nString(ALL, DATABASE);
    }

    public static String init() {
        return I18nResourceBundle.i18nString(INIT);
    }

    public static String connectInitIng() {
        return I18nResourceBundle.i18nString(CONNECT, INIT, ING);
    }

    public static String connectInitSuccess() {
        return I18nResourceBundle.i18nString(CONNECT, INIT, SUCCESS);
    }

    public static String connectInitFail() {
        return I18nResourceBundle.i18nString(CONNECT, INIT, FAIL);
    }

    public static String transportFail() {
        return I18nResourceBundle.i18nString(TRANSPORT, FAIL);
    }

    public static String transportCancel() {
        return I18nResourceBundle.i18nString(TRANSPORT, CANCEL);
    }

    public static String transportSuccess() {
        return I18nResourceBundle.i18nString(TRANSPORT, SUCCESS);
    }

    public static String transportFinish() {
        return I18nResourceBundle.i18nString(TRANSPORT, FINISH);
    }

    public static String initFail() {
        return I18nResourceBundle.i18nString(INIT, ING);
    }

    public static String target() {
        return I18nResourceBundle.i18nString(TARGET);
    }

    public static String targetConnect() {
        return I18nResourceBundle.i18nString(TARGET, CONNECT);
    }

    public static String source() {
        return I18nResourceBundle.i18nString(SOURCE);
    }

    public static String sourceConnect() {
        return I18nResourceBundle.i18nString(SOURCE, CONNECT);
    }

    public static String only() {
        return I18nResourceBundle.i18nString(ONLY);
    }

    public static String single() {
        return I18nResourceBundle.i18nString(SINGLE);
    }

    public static String onlySupportSingleFile() {
        return I18nResourceBundle.i18nString(ONLY, SUPPORT, SINGLE, FILE);
    }

    public static String choose() {
        return I18nResourceBundle.i18nString(CHOOSE);
    }

    public static String chooseFile() {
        return I18nResourceBundle.i18nString(CHOOSE, FILE);
    }

    public static String never() {
        return I18nResourceBundle.i18nString(NEVER);
    }

    public static String expire() {
        return I18nResourceBundle.i18nString(EXPIRE);
    }

    public static String neverExpire() {
        return I18nResourceBundle.i18nString(NEVER, EXPIRE);
    }

    public static String delete() {
        return I18nResourceBundle.i18nString(DELETE);
    }

    public static String keys() {
        return I18nResourceBundle.i18nString(KEYS);
    }

    public static String deleteKey() {
        return I18nResourceBundle.i18nString(DELETE, KEY);
    }

    public static String deleteKeys() {
        return I18nResourceBundle.i18nString(DELETE, KEYS);
    }

    public static String deleted() {
        return I18nResourceBundle.i18nString(DELETED);
    }

    public static String found() {
        return I18nResourceBundle.i18nString(FOUND);
    }

    public static String currentDatabase() {
        return I18nResourceBundle.i18nString(CURRENT, DATABASE);
    }

    public static String deleteData() {
        return I18nResourceBundle.i18nString(DELETE, DATA);
    }

    public static String invalidData() {
        return I18nResourceBundle.i18nString(INVALID, DATA);
    }

    public static String copySuccess() {
        return I18nResourceBundle.i18nString(COPY, SUCCESS);
    }

    public static String connectSuccess() {
        return I18nResourceBundle.i18nString(CONNECT, SUCCESS);
    }

    public static String connectFail() {
        return I18nResourceBundle.i18nString(CONNECT, FAIL);
    }

    public static String connectIng() {
        return I18nResourceBundle.i18nString(CONNECT, ING);
    }

    public static String connectTesting() {
        return I18nResourceBundle.i18nString(CONNECT, TEST, ING);
    }

    public static String restoreData() {
        return I18nResourceBundle.i18nString(RESTORE, DATA);
    }

    public static String quit() {
        return I18nResourceBundle.i18nString(QUIT);
    }

    public static String connectStart() {
        return I18nResourceBundle.i18nString(CONNECT, START);
    }

    public static String invalidOperation() {
        return I18nResourceBundle.i18nString(INVALID, OPERATION);
    }

    public static String userNameCanNotEmpty() {
        return I18nResourceBundle.i18nString(USER_NAME, CAN_NOT, EMPTY);
    }

    public static String passwordCanNotEmpty() {
        return I18nResourceBundle.i18nString(PASSWORD, CAN_NOT, EMPTY);
    }

    public static String invalidPerms() {
        return I18nResourceBundle.i18nString(INVALID, PERMS);
    }

    public static String perms() {
        return I18nResourceBundle.i18nString(PERMS);
    }

    public static String nameCanNotEmpty() {
        return I18nResourceBundle.i18nString(NAME, CAN_NOT, EMPTY);
    }

    public static String trayNotSupport() {
        return I18nResourceBundle.i18nString(TRAY, NOT, SUPPORT);
    }

    public static String name() {
        return I18nResourceBundle.i18nString(NAME);
    }

    public static String value() {
        return I18nResourceBundle.i18nString(VALUE);
    }

    public static String cost() {
        return I18nResourceBundle.i18nString(COST);
    }

    public static String env() {
        return I18nResourceBundle.i18nString(ENV);
    }

    public static String soft() {
        return I18nResourceBundle.i18nString(SOFT);
    }

    public static String latitude() {
        return I18nResourceBundle.i18nString(LATITUDE);
    }

    public static String longitude() {
        return I18nResourceBundle.i18nString(LONGITUDE);
    }

    public static String host() {
        return I18nResourceBundle.i18nString(HOST);
    }

    public static String port() {
        return I18nResourceBundle.i18nString(PORT);
    }

    public static String userName() {
        return I18nResourceBundle.i18nString(USER_NAME);
    }

    public static String password() {
        return I18nResourceBundle.i18nString(PASSWORD);
    }

    public static String readonlyMode() {
        return I18nResourceBundle.i18nString(READONLY_MODE);
    }
}
