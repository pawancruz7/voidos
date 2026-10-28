#!/usr/bin/env python3

import json

class VoidStorage:
    def __init__(self, store_path: str = 'state.json'):
        self.store_path = store_path

    def persist(self, data: dict) -        with open(self.store_path, 'w', encoding='utf-8') as f:
            json.dump(data, f, indent=4)
