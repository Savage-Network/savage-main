# Como Instalar WorldEdit no Servidor

## Passo a Passo

### 1. Baixar o WorldEdit

Baixe o WorldEdit 6.1.9 para Bukkit/Spigot 1.8:
- **Link direto**: https://dev.bukkit.org/projects/worldedit/files/956653
- **Ou SpigotMC**: https://www.spigotmc.org/resources/worldedit.13932/

### 2. Instalar no Servidor

1. Copie o arquivo `worldedit-bukkit-6.1.9.jar` para a pasta `plugins/` do seu servidor
2. Reinicie o servidor
3. Pronto! O WorldEdit está instalado

### 3. Verificar Instalação

Após reiniciar o servidor, digite no console ou in-game:
```
/version WorldEdit
```

Se aparecer a versão, está funcionando!

## Comandos Principais

### Seleção
- `//wand` - Pega o machado de seleção (madeira)
  - Clique esquerdo = primeira posição
  - Clique direito = segunda posição
- `//pos1` - Define pos1 onde você está
- `//pos2` - Define pos2 onde você está
- `//hpos1` - Define pos1 no bloco que você está olhando
- `//hpos2` - Define pos2 no bloco que você está olhando

### Copiar e Colar
- `//copy` - Copia a seleção
- `//cut` - Corta a seleção
- `//paste` - Cola
- `//paste -a` - Cola sem ar
- `//rotate 90` - Rotaciona 90 graus
- `//flip` - Espelha

### Modificar Blocos
- `//set <bloco>` - Preenche com um bloco
  - Exemplo: `//set stone`
  - Exemplo: `//set 1` (stone por ID)
- `//replace <de> <para>` - Substitui blocos
  - Exemplo: `//replace dirt stone`
- `//walls <bloco>` - Cria paredes
- `//faces <bloco>` - Cria faces (paredes + teto + chão)

### Schematics
- `//schematic save <nome>` ou `//schem save <nome>` - Salva
- `//schematic load <nome>` ou `//schem load <nome>` - Carrega
- `//schematic list` ou `//schem list` - Lista schematics
- `//paste` - Cola o schematic carregado

**Localização dos schematics**: `plugins/WorldEdit/schematics/`

### Geração
- `//sphere <bloco> <raio>` - Esfera
- `//hsphere <bloco> <raio>` - Esfera oca
- `//cyl <bloco> <raio> <altura>` - Cilindro
- `//hcyl <bloco> <raio> <altura>` - Cilindro oco
- `//pyramid <bloco> <tamanho>` - Pirâmide

### Utilitários
- `//undo` - Desfaz
- `//redo` - Refaz
- `//drain <raio>` - Remove água/lava
- `//fixwater <raio>` - Corrige água
- `//fixlava <raio>` - Corrige lava
- `//snow <raio>` - Adiciona neve
- `//thaw <raio>` - Remove neve/gelo
- `//desel` - Remove seleção

### Expansão de Seleção
- `//expand <quantidade> <direção>` - Expande
  - Exemplo: `//expand 10 up`
  - Exemplo: `//expand 5 north`
- `//expand vert` - Expande do bedrock ao céu
- `//contract <quantidade>` - Contrai

## Permissões

Para dar todas as permissões do WorldEdit a um jogador:
```
/lp user <nome> permission set worldedit.* true
```

Ou para um grupo:
```
/lp group admin permission set worldedit.* true
```

### Permissões Específicas
- `worldedit.clipboard.*` - Comandos de clipboard
- `worldedit.schematic.*` - Comandos de schematic
- `worldedit.selection.*` - Comandos de seleção
- `worldedit.region.*` - Comandos de região
- `worldedit.generation.*` - Comandos de geração

## Configuração

O arquivo de configuração fica em: `plugins/WorldEdit/config.yml`

Configurações úteis:
```yaml
# Limite de blocos que pode modificar
default-max-changed-blocks: 1000000

# Limite de polígonos
default-max-polygon-points: 20

# Histórico de undo
history-size: 15

# Usar inventário do jogador
use-inventory:
  enable: false
```

## Dicas

1. **Misturar blocos**: `//set 50% stone,50% cobblestone`
2. **Padrões**: `//set ##jungle` (madeira de selva)
3. **Máscaras**: `//gmask stone` (só modifica pedra)
4. **Aumentar limite**: `//limit 999999`
5. **Selecionar tudo**: `//expand vert` depois `//expand 999 north,south,east,west`

## Exemplos Práticos

### Criar uma casa básica
```
1. //wand
2. Selecione a área (clique esquerdo e direito)
3. //walls stone
4. //faces stone
```

### Copiar e colar uma construção
```
1. //wand
2. Selecione a construção
3. //copy
4. Vá para onde quer colar
5. //paste
```

### Salvar como schematic
```
1. //wand
2. Selecione a construção
3. //copy
4. //schem save minha_casa
```

### Carregar schematic
```
1. //schem load minha_casa
2. Vá para onde quer colar
3. //paste
```

## Troubleshooting

### "You don't have permission"
- Dê a permissão `worldedit.*` ao jogador/grupo

### "Maximum blocks changed in one operation"
- Use `//limit <número>` para aumentar o limite

### Schematic não encontrado
- Verifique se está em `plugins/WorldEdit/schematics/`
- Use `//schem list` para ver os disponíveis

## Links Úteis

- **Wiki oficial**: https://worldedit.enginehub.org/en/latest/
- **Comandos completos**: https://worldedit.enginehub.org/en/latest/commands/
- **Download**: https://dev.bukkit.org/projects/worldedit
