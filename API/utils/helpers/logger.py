import inspect
import logging
import threading


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
        # mergem înapoi în stack până găsim metoda reală (cu self)
        frame = inspect.currentframe()
        caller = None

        for _ in range(6):  # limită de siguranță (evită buclă infinită)
            frame = frame.f_back
            if not frame:
                break

            func_name = frame.f_code.co_name
            module_name = frame.f_globals.get('__name__', '<module>')
            if "self" in frame.f_locals:
                cls_name = frame.f_locals["self"].__class__.__name__
                caller = f"{module_name}.{cls_name}.{func_name}"
                break

        # fallback dacă nu găsește nicio clasă
        if not caller:
            func_name = frame.f_code.co_name if frame else "<unknown>"
            module_name = frame.f_globals.get('__name__', '<module>') if frame else "<module>"
            caller = f"{module_name}.{func_name}"

        extra = {"caller": caller}
        self.logger.log(level, msg, *args, extra=extra, stacklevel=3, **kwargs)

    # metode standard
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
