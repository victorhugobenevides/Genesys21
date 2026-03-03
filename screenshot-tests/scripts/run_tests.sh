#!/bin/bash
# Script para executar todos os testes de screenshot localmente

set -e

echo "=================================="
echo "Screenshot Tests - Genesys21"
echo "=================================="
echo ""

# Cores para output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Função para printar com cor
print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Navegar para a raiz do projeto
cd "$(dirname "$0")/../.."

print_status "Limpando build anterior..."
./gradlew :screenshot-tests:clean

print_status "Executando testes de screenshot..."
./gradlew :screenshot-tests:testDebugUnitTest --info

if [ $? -eq 0 ]; then
    print_status "Todos os testes passaram! ✓"
    print_status "Screenshots gerados em: screenshot-tests/build/reports/paparazzi/debug/"
    
    # Contar screenshots gerados
    SNAPSHOT_COUNT=$(find screenshot-tests/src/test/snapshots -name "*.png" 2>/dev/null | wc -l)
    print_status "Total de snapshots: $SNAPSHOT_COUNT"
else
    print_error "Alguns testes falharam! ✗"
    print_warning "Verifique os diffs em: screenshot-tests/out/failures/"
    exit 1
fi

echo ""
echo "=================================="
echo "Para atualizar as referências:"
echo "./gradlew :screenshot-tests:recordPaparazziDebug"
echo "=================================="
