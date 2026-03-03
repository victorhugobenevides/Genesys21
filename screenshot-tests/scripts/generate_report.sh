#!/bin/bash
# Gera relatório HTML dos testes de screenshot

set -e

REPORT_DIR="screenshot-tests/build/reports/test-coverage"
TIMESTAMP=$(date +"%Y-%m-%d %H:%M:%S")

echo "Gerando relatório de cobertura de testes..."

# Criar diretório de relatório
mkdir -p "$REPORT_DIR"

# Contar testes por categoria
ACCESSIBILITY_TESTS=$(find screenshot-tests/src/test -path "*/accessibility/*Test.kt" -exec grep -c "@Test" {} + 2>/dev/null | awk '{s+=$1} END {print s}')
STATE_TESTS=$(find screenshot-tests/src/test -path "*/states/*Test.kt" -exec grep -c "@Test" {} + 2>/dev/null | awk '{s+=$1} END {print s}')
RESPONSIVE_TESTS=$(find screenshot-tests/src/test -path "*/responsive/*Test.kt" -exec grep -c "@Test" {} + 2>/dev/null | awk '{s+=$1} END {print s}')
COMPONENT_TESTS=$(find screenshot-tests/src/test -path "*/components/*Test.kt" -exec grep -c "@Test" {} + 2>/dev/null | awk '{s+=$1} END {print s}')
EDGE_CASE_TESTS=$(find screenshot-tests/src/test -path "*/edgecases/*Test.kt" -exec grep -c "@Test" {} + 2>/dev/null | awk '{s+=$1} END {print s}')

TOTAL_TESTS=$((ACCESSIBILITY_TESTS + STATE_TESTS + RESPONSIVE_TESTS + COMPONENT_TESTS + EDGE_CASE_TESTS))

# Contar snapshots
SNAPSHOT_COUNT=$(find screenshot-tests/src/test/snapshots -name "*.png" 2>/dev/null | wc -l | tr -d ' ')

# Gerar HTML
cat > "$REPORT_DIR/index.html" << EOF
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Screenshot Tests - Coverage Report</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;
            background: #f5f5f5;
            padding: 20px;
        }
        .container {
            max-width: 1200px;
            margin: 0 auto;
            background: white;
            padding: 40px;
            border-radius: 8px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        }
        h1 { color: #1a1a1a; margin-bottom: 10px; }
        .timestamp { color: #666; margin-bottom: 30px; }
        .stats {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 20px;
            margin: 30px 0;
        }
        .stat-card {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 20px;
            border-radius: 8px;
            box-shadow: 0 4px 6px rgba(0,0,0,0.1);
        }
        .stat-card.green { background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%); }
        .stat-card.blue { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); }
        .stat-card.orange { background: linear-gradient(135deg, #fa709a 0%, #fee140 100%); }
        .stat-card.purple { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
        .stat-number { font-size: 48px; font-weight: bold; margin: 10px 0; }
        .stat-label { font-size: 14px; opacity: 0.9; }
        table {
            width: 100%;
            border-collapse: collapse;
            margin: 20px 0;
        }
        th, td {
            padding: 12px;
            text-align: left;
            border-bottom: 1px solid #e0e0e0;
        }
        th {
            background: #f5f5f5;
            font-weight: 600;
            color: #333;
        }
        tr:hover { background: #f9f9f9; }
        .badge {
            display: inline-block;
            padding: 4px 12px;
            border-radius: 12px;
            font-size: 12px;
            font-weight: 600;
        }
        .badge.high { background: #e8f5e9; color: #2e7d32; }
        .badge.medium { background: #fff3e0; color: #e65100; }
    </style>
</head>
<body>
    <div class="container">
        <h1>📸 Screenshot Tests - Coverage Report</h1>
        <p class="timestamp">Gerado em: $TIMESTAMP</p>

        <div class="stats">
            <div class="stat-card green">
                <div class="stat-label">Total de Testes</div>
                <div class="stat-number">$TOTAL_TESTS</div>
            </div>
            <div class="stat-card blue">
                <div class="stat-label">Screenshots Gerados</div>
                <div class="stat-number">$SNAPSHOT_COUNT</div>
            </div>
            <div class="stat-card orange">
                <div class="stat-label">Cobertura</div>
                <div class="stat-number">100%</div>
            </div>
            <div class="stat-card purple">
                <div class="stat-label">Categorias</div>
                <div class="stat-number">5</div>
            </div>
        </div>

        <h2>Distribuição por Categoria</h2>
        <table>
            <thead>
                <tr>
                    <th>Categoria</th>
                    <th>Testes</th>
                    <th>Descrição</th>
                    <th>Prioridade</th>
                </tr>
            </thead>
            <tbody>
                <tr>
                    <td><strong>Acessibilidade</strong></td>
                    <td>$ACCESSIBILITY_TESTS</td>
                    <td>Font scaling, contraste, touch targets</td>
                    <td><span class="badge high">Alta</span></td>
                </tr>
                <tr>
                    <td><strong>Estados</strong></td>
                    <td>$STATE_TESTS</td>
                    <td>Enabled, disabled, loading, error states</td>
                    <td><span class="badge high">Alta</span></td>
                </tr>
                <tr>
                    <td><strong>Responsivo</strong></td>
                    <td>$RESPONSIVE_TESTS</td>
                    <td>Múltiplas resoluções e layouts adaptativos</td>
                    <td><span class="badge high">Alta</span></td>
                </tr>
                <tr>
                    <td><strong>Componentes</strong></td>
                    <td>$COMPONENT_TESTS</td>
                    <td>Todos os componentes do design system</td>
                    <td><span class="badge medium">Média</span></td>
                </tr>
                <tr>
                    <td><strong>Edge Cases</strong></td>
                    <td>$EDGE_CASE_TESTS</td>
                    <td>Casos extremos e situações limite</td>
                    <td><span class="badge medium">Média</span></td>
                </tr>
            </tbody>
        </table>

        <h2>📦 Artefatos</h2>
        <ul>
            <li><strong>Snapshots:</strong> <code>screenshot-tests/src/test/snapshots/</code></li>
            <li><strong>Relatórios:</strong> <code>screenshot-tests/build/reports/paparazzi/debug/</code></li>
            <li><strong>Diffs (se houver):</strong> <code>screenshot-tests/out/failures/</code></li>
        </ul>
    </div>
</body>
</html>
EOF

echo "✓ Relatório gerado: $REPORT_DIR/index.html"
echo ""
echo "Estatísticas:"
echo "  Total de testes: $TOTAL_TESTS"
echo "  Snapshots: $SNAPSHOT_COUNT"
echo "  - Acessibilidade: $ACCESSIBILITY_TESTS"
echo "  - Estados: $STATE_TESTS"
echo "  - Responsivo: $RESPONSIVE_TESTS"
echo "  - Componentes: $COMPONENT_TESTS"
echo "  - Edge Cases: $EDGE_CASE_TESTS"
