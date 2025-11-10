import logging
import threading
import inspect


class Logger:
    _instance = None
    _lock = threading.Lock()

    def __new__(cls, *args, **kwargs):
        if not cls._instance:
            with cls._lock:
                if not cls._instance:
                    cls._instance = super().__new__(cls)
                    cls._instance._initialize(*args, **kwargs)
        return cls._instance

    def _initialize(self, level=logging.DEBUG):
        self.logger = logging.getLogger("Logger")
        self.logger.setLevel(level)
        self.logger.propagate = False

        if not self.logger.handlers:
            formatter = logging.Formatter(
                fmt='[%(levelname)s] [%(caller)s] %(message)s'
            )
            console_handler = logging.StreamHandler()
            console_handler.setFormatter(formatter)
            self.logger.addHandler(console_handler)

    def _log(self, level, msg, *args, **kwargs):
        frame = inspect.currentframe().f_back.f_back
        func_name = frame.f_code.co_name
        cls_name = None
        if "self" in frame.f_locals:
            cls_name = frame.f_locals["self"].__class__.__name__

        caller = f"{cls_name}.{func_name}" if cls_name else func_name

        extra = {"caller": caller}
        self.logger.log(level, msg, *args, extra=extra, stacklevel=3, **kwargs)

    def debug(self, msg, *args, **kwargs):
        self._log(logging.DEBUG, msg, *args, **kwargs)

    def info(self, msg, *args, **kwargs):
        self._log(logging.INFO, msg, *args, **kwargs)

    def warning(self, msg, *args, **kwargs):
        self._log(logging.WARNING, msg, *args, **kwargs)

    def error(self, msg, *args, **kwargs):
        self._log(logging.ERROR, msg, *args, **kwargs)

    def critical(self, msg, *args, **kwargs):
        self._log(logging.CRITICAL, msg, *args, **kwargs)
