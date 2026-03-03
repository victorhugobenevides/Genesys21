#!/bin/bash
# Script de validação para CI/CD

set -e

echo "=================================="
echo "CI/CD Screenshot Validation"
echo "=================================="

# Configurações
MAX_PERCENT_DIFFERENCE=2.0  # Máximo 2% de diferença permitida
FAIL_ON_MISSING_GOLDEN=true

# Navegar para a raiz do projeto
cd "$(dirname "$0")/../.."

echo "[CI] Verificando environment..."
if [ -z "$CI" ]; then
    echo "[WARNING] Não detectado ambiente de CI. Executando localmente."
fi

echo "[CI] Limpando cache..."
./gradlew :screenshot-tests:clean

echo "[CI] Executando verificação de screenshots..."
if ./gradlew :screenshot-tests:verifyPaparazziDebug --info; then
    echo "[CI] ✓ Screenshots verificados com sucesso!"
    exit 0
else
    echo "[CI] ✗ Falha na verificação de screenshots!"
    
    # Verificar se existem diffs
    if [ -d "screenshot-tests/out/failures" ]; then
        echo "[CI] Diffs detectados:"
        find screenshot-tests/out/failures -name "*.png" | while read file; do
            echo "  - $(basename "$file")"
        done
        
        echo ""
        echo "[CI] Para visualizar os diffs:"
        echo "  1. Baixe os artefatos do CI"
        echo "  2. Verifique screenshot-tests/out/failures/"
        echo ""
        echo "[CI] Se as mudanças são intencionais:"
        echo "  ./gradlew :screenshot-tests:recordPaparazziDebug"
        echo "  git add screenshot-tests/src/test/snapshots/"
        echo "  git commit -m 'test: update screenshot references'"
    fi
    
    exit 1
fi
