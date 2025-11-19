import logging
import threading


class Logger:
    """
    Thread-safe singleton logger used across the application.

    Args:
        None

    Returns:
        Logger shared logger instance

    Exceptions:
        None
    """
    _instance = None
    _lock = threading.Lock()
    
    def __new__(cls, *args, **kwargs):
        """
        Ensures only one Logger instance exists.

        Args:
            *args unused
            **kwargs unused

        Returns:
            Logger singleton instance

        Exceptions:
            None
        """
        if not cls._instance:
            with cls._lock:
                if not cls._instance:
                    cls._instance = super().__new__(cls)
                    cls._instance._initialize(*args, **kwargs)
        return cls._instance

    def _initialize(self, level=logging.DEBUG):
        """
        Configures the logger with console output and formatting.

        Args:
            level (int) logging level

        Returns:
            None

        Exceptions:
            None
        """
        self.logger = logging.getLogger("Logger")
        self.logger.setLevel(level)
        self.logger.propagate = False

        if not self.logger.handlers:
            formatter = logging.Formatter(
                fmt='[%(levelname)s] [%(funcName)s] %(message)s'
            )
            console_handler = logging.StreamHandler()
            console_handler.setFormatter(formatter)
            self.logger.addHandler(console_handler)

    def debug(self, msg, *args, **kwargs):
        """
        Logs a debug message.

        Args:
            msg (str) log message

        Returns:
            None

        Exceptions:
            None
        """
        self.logger.debug(msg, *args, stacklevel=2, **kwargs)

    def info(self, msg, *args, **kwargs):
        """
        Logs an info message.

        Args:
            msg (str) log message

        Returns:
            None

        Exceptions:
            None
        """
        self.logger.info(msg, *args, stacklevel=2, **kwargs)

    def warning(self, msg, *args, **kwargs):
        """
        Logs a warning message.

        Args:
            msg (str) log message

        Returns:
            None

        Exceptions:
            None
        """
        self.logger.warning(msg, *args, stacklevel=2, **kwargs)

    def error(self, msg, *args, **kwargs):
        """
        Logs an error message.

        Args:
            msg (str) log message

        Returns:
            None

        Exceptions:
            None
        """
        self.logger.error(msg, *args, stacklevel=2, **kwargs)

    def critical(self, msg, *args, **kwargs):
        """
        Logs a critical message.

        Args:
            msg (str) log message

        Returns:
            None

        Exceptions:
            None
        """
        self.logger.critical(msg, *args, stacklevel=2, **kwargs)
