def log_analyzer(logs):
    result = {"INFO": 0, "WARN": 0, "ERROR": 0}
    for log in logs:
        if log in result:
            result[log] += 1
    return result

# Testcases
print(log_analyzer(["INFO", "ERROR", "INFO", "WARN", "ERROR"]))
# Expected: {'INFO': 2, 'WARN': 1, 'ERROR': 2}

print(log_analyzer(["INFO", "INFO", "INFO"]))
# Expected: {'INFO': 3, 'WARN': 0, 'ERROR': 0}
