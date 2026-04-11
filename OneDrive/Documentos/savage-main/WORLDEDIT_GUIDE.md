# Guia de WorldEdit no Servidor

## Instalação

### Para usar o WorldEdit no servidor:

1. **Baixe o WorldEdit 6.1.9 para Bukkit 1.8**:
   - Link: https://dev.bukkit.org/projects/worldedit/files/956653
   - Ou: https://www.spigotmc.org/resources/worldedit.13932/

2. **Coloque o arquivo na pasta libs do projeto**:
   - Copie `worldedit-bukkit-6.1.9.jar` para a pasta `libs/` (junto com spigot.jar e ProtocolLib.jar)

3. **Descomente a linha no build.gradle.kts**:
   - Abra `core-bukkit/build.gradle.kts`
   - Descomente a linha: `// compileOnly(files("../libs/worldedit-bukkit-6.1.9.jar"))`

4. **Adicione as classes utilitárias** (fornecidas abaixo)

5. **Coloque o plugin no servidor**:
   - Copie `worldedit-bukkit-6.1.9.jar` para `server/plugins/`
   - Reinicie o servidor

## Classes Utilitárias (Adicionar depois)

Quando o WorldEdit estiver instalado, você pode adicionar estas classes:

### SchematicUtil.java
Localização: `core-bukkit/src/main/java/com/minecraft/savagemc/core/bukkit/util/SchematicUtil.java`

```java
// Código fornecido anteriormente - veja o histórico do chat
```

### SchematicCommand.java
Localização: `core-bukkit/src/main/java/com/minecraft/savagemc/core/bukkit/command/SchematicCommand.java`

```java
// Código fornecido anteriormente - veja o histórico do chat
```

## Comandos Básicos do WorldEdit

### Seleção de Região
- `//wand` - Pega o machado de seleção (clique esquerdo = pos1, clique direito = pos2)
- `//pos1` - Define a primeira posição na sua localização atual
- `//pos2` - Define a segunda posição na sua localização atual
- `//expand <quantidade> <direção>` - Expande a seleção (ex: `//expand 10 up`)
- `//contract <quantidade> <direção>` - Contrai a seleção

### Copiar e Colar
- `//copy` - Copia a região selecionada
- `//cut` - Corta a região selecionada
- `//paste` - Cola a região copiada
- `//paste -a` - Cola sem ar (não substitui blocos existentes por ar)
- `//rotate <graus>` - Rotaciona a clipboard (90, 180, 270)
- `//flip <direção>` - Espelha a clipboard (north, south, east, west, up, down)

### Schematics
- `//schem save <nome>` - Salva a região copiada como schematic
- `//schem load <nome>` - Carrega um schematic
- `//schem list` - Lista todos os schematics disponíveis

### Modificação de Blocos
- `//set <bloco>` - Preenche a região com um bloco (ex: `//set stone`)
- `//replace <bloco_antigo> <bloco_novo>` - Substitui blocos
- `//walls <bloco>` - Cria paredes na região
- `//faces <bloco>` - Cria faces (paredes + teto + chão)
- `//overlay <bloco>` - Coloca blocos por cima da região

### Geração
- `//sphere <bloco> <raio>` - Cria uma esfera
- `//hsphere <bloco> <raio>` - Cria uma esfera oca
- `//cyl <bloco> <raio> <altura>` - Cria um cilindro
- `//hcyl <bloco> <raio> <altura>` - Cria um cilindro oco
- `//pyramid <bloco> <tamanho>` - Cria uma pirâmide

### Utilitários
- `//undo` - Desfaz a última ação
- `//redo` - Refaz a última ação desfeita
- `//drain <raio>` - Remove água/lava em um raio
- `//fixwater <raio>` - Corrige água em um raio
- `//fixlava <raio>` - Corrige lava em um raio
- `//snow <raio>` - Adiciona neve em um raio
- `//thaw <raio>` - Remove neve/gelo em um raio

## Comando Customizado do Servidor

### /schematic
- `/schematic paste <nome>` - Cola um schematic na sua localização
- `/schematic paste <nome> noair` - Cola sem ar
- Permissão: `savage.schematic`

## Uso Programático

### SchematicUtil

```java
import com.minecraft.savagemc.core.bukkit.util.SchematicUtil;
import org.bukkit.Location;

// Colar schematic
Location location = player.getLocation();
SchematicUtil.pasteSchematic("meu_schematic", location);

// Colar sem ar
SchematicUtil.pasteSchematic("meu_schematic", location, true);

// Salvar schematic
Location pos1 = new Location(world, 0, 64, 0);
Location pos2 = new Location(world, 10, 74, 10);
SchematicUtil.saveSchematic(world, pos1, pos2, "novo_schematic");
```

## Localização dos Schematics

Os schematics ficam salvos em: `plugins/WorldEdit/schematics/`

## Dicas

1. Use `//limit <número>` para aumentar o limite de blocos que pode modificar
2. Use `//gmask <bloco>` para modificar apenas certos blocos
3. Use `//desel` para desselecionar a região atual
4. Combine comandos: `//set 50% stone,50% cobblestone` para misturar blocos

## Permissões

- `worldedit.*` - Acesso total ao WorldEdit
- `worldedit.clipboard.*` - Comandos de clipboard
- `worldedit.schematic.*` - Comandos de schematic
- `savage.schematic` - Comando customizado do servidor
