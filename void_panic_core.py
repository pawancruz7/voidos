#!/usr/bin/env python3

import sys

class VoidPanic:
    @staticmethod
    def trigger_protocol(reason: str) -        sys.stderr.write(f'[CRITICAL] Panic protocol initiated: {reason}\n')
        sys.exit(1)
