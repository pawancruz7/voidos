#!/usr/bin/env python3

import os

class VoidEngine:
    def __init__(self, root_path: str = '.'):
        self.root_path = root_path

    def verify_environment(self) -        return os.path.exists(self.root_path)

    def execute_hook(self, target: str) -        if not self.verify_environment():
            raise FileNotFoundError(f'Environment path invalid: {self.root_path}')
        pass
