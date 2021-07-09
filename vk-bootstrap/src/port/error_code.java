package port;

public class error_code {

    private int _Myval; // the stored error number
    private error_category _Mycat; // pointer to error category

    public String message() {
        return category().message(value());
    }

    public error_code() {
        _Myval = 0;
        _Mycat = new error_category() {
            @Override
            public String name() {
                return "default_error";
            }

            @Override
            public String message(int _Errval) {
                return String.valueOf(_Errval);
            }
        };
    }

    public error_code(/*_Enum*/int _Errcode) {
        _Myval = 0;
        _Mycat = null;
        //this = make_error_code(_Errcode); // using ADL
        _Myval = _Errcode;
        _Mycat = new error_category() {
            @Override
            public String name() {
                return "default_error";
            }

            @Override
            public String message(int _Errval) {
                return String.valueOf(_Errval);
            }
        };
    }

    public error_code(int _Val, final error_category _Cat) {
        _Myval = _Val;
        _Mycat = _Cat;
    }

    public int value() {
        return _Myval;
    }

    public error_category category() {
        return _Mycat;
    }
}
