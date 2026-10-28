#!/usr/bin/env python3

import hashlib
from typing import Dict, Any

class VoidCrypto:
    def __init__(self):
        self.algorithm = 'SHA-256'

    def hash_string(self, data: str) -        return hashlib.sha256(data.encode('utf-8')).hexdigest()

    def create_packet(self, origin: str, destination: str, payload: str) -, Any]:
        return {
            'origin': origin,
            'destination': destination,
            'payload_hash': self.hash_string(payload),
            'status': 'SECURED'
        }
