from mcresources import ResourceManager, ItemContext, BlockContext, block_states
from mcresources import utils, loot_tables

from constants import *


def generate(rm: ResourceManager):
    bot_variants = four_rotations_mp('firmalife:block/oven_fire', (90, None, 180, 270), 'lit', True)
    for stage in ('bricks', 'clay'):
        tex = 'minecraft:block/bricks' if stage == 'bricks' else 'firmalife:block/unfired_bricks'
        for part in ('bottom', 'top'):
            rm.block_model('oven_%s_%s' % (part, stage), parent='firmalife:block/oven_%s' % part, textures={'0': tex, 'particle': tex})
        rm.block_model('oven_particle_%s' % stage, parent='tfc:block/empty', textures={'particle': tex})
    for i in range(1, 5):
        bot_variants += four_rotations_mp('firmalife:block/oven_logs_%s' % i, (90, None, 180, 270), 'logs', i)

    rm.block_model('oven_chimney_bricks', parent='firmalife:block/oven_chimney', textures={'cured': 'minecraft:block/bricks', 'particle': 'minecraft:block/bricks'})
    rm.block_model('oven_chimney_clay', parent='firmalife:block/oven_chimney', textures={'cured': 'firmalife:block/unfired_bricks', 'particle': 'firmalife:block/unfired_bricks'})
    for stage in ('bricks', 'clay'):
        pref = 'cured_' if stage == 'bricks' else ''
        last_bot_variants = bot_variants.copy()
        last_bot_variants += four_rotations_mp_free('firmalife:block/oven_bottom_%s' % stage, (90, None, 180, 270))
        last_bot_variants += [{'model': 'firmalife:block/oven_particle_%s' % stage}]
        rm.blockstate_multipart('%soven_bottom' % pref, *last_bot_variants).with_lang(lang('%sbottom oven', pref)).with_tag('firmalife:oven_blocks').with_block_loot('firmalife:%soven_bottom' % pref)
        rm.item_model('%soven_bottom' % pref, parent='firmalife:block/oven_bottom_%s' % stage, no_textures=True)
        rm.blockstate('%soven_top' % pref, variants={**four_rotations('firmalife:block/oven_top_%s' % stage, (90, None, 180, 270))}).with_lang(lang('%stop oven', pref)).with_tag('firmalife:oven_blocks').with_block_loot('firmalife:%soven_top' % pref)
        rm.item_model('%soven_top' % pref, parent='firmalife:block/oven_top_%s' % stage, no_textures=True)
        rm.blockstate('%soven_chimney' % pref, model='firmalife:block/oven_chimney_%s' % stage).with_lang(lang('%soven chimney', pref)).with_tag('firmalife:oven_blocks').with_tag('firmalife:chimneys').with_block_loot('firmalife:%soven_chimney' % pref)
        rm.item_model('%soven_chimney' % pref, parent='firmalife:block/oven_chimney_%s' % stage, no_textures=True)

    rm.blockstate('drying_mat', model='firmalife:block/drying_mat').with_item_model().with_tag('tfc:mineable_with_sharp_tool').with_lang(lang('drying mat'))

    for fruit in TFC_FRUITS:
        rm.item_model(('not_dried', fruit), 'tfc:item/food/%s' % fruit)
        rm.item_model(('dried', fruit), 'firmalife:item/dried/dried_%s' % fruit)
        item_model_property(rm, 'tfc:food/%s' % fruit, [{'predicate': {'firmalife:dry': 1}, 'model': 'firmalife:item/dried/%s' % fruit}], {'parent': 'firmalife:item/not_dried/%s' % fruit})

    for greenhouse in GREENHOUSES:
        greenhouse_slab(rm, greenhouse, 'firmalife:block/greenhouse/%s' % greenhouse, 'firmalife:block/greenhouse/%s_glass' % greenhouse)
        greenhouse_stairs(rm, greenhouse, 'firmalife:block/greenhouse/%s' % greenhouse, 'firmalife:block/greenhouse/%s_glass' % greenhouse)
        greenhouse_wall(rm, greenhouse, 'firmalife:block/greenhouse/%s' % greenhouse, 'firmalife:block/greenhouse/%s_glass' % greenhouse)
        greenhouse_door(rm, greenhouse, 'firmalife:block/greenhouse/%s_door_bottom' % greenhouse, 'firmalife:block/greenhouse/%s_door_top' % greenhouse)

    for planter in ('quad_planter', 'large_planter'):
        for state in ('wet', 'dry'):
            rm.block_model('%s_%s' % (planter, state), parent='firmalife:block/%s' % planter, textures={'soil': 'firmalife:block/potting_soil_%s' % state})
        rm.blockstate(planter, variants={
            'watered=true': {'model': 'firmalife:block/%s_wet' % planter},
            'watered=false': {'model': 'firmalife:block/%s_dry' % planter}
        }).with_lang(lang(planter)).with_block_loot('firmalife:%s' % planter).with_tag('minecraft:mineable/axe').with_tag('minecraft:mineable/pickaxe')
        rm.item_model(planter, parent='firmalife:block/%s_dry' % planter)

    rm.blockstate('climate_station', variants={
        'stasis=true': {'model': 'firmalife:block/climate_station_valid'},
        'stasis=false': {'model': 'firmalife:block/climate_station_invalid'}
    }).with_lang(lang('climate station')).with_tag('minecraft:mineable/axe').with_block_loot('firmalife:climate_station')
    rm.item_model('climate_station', parent='firmalife:block/climate_station_invalid')
    for variant in ('valid', 'invalid'):
        tex = 'firmalife:block/greenhouse/climate_station/%s' % variant
        rm.block_model('firmalife:climate_station_%s' % variant, {'west': tex, 'east': tex, 'north': tex, 'south': tex, 'particle': tex, 'up': 'firmalife:block/greenhouse/climate_station/top', 'down': 'firmalife:block/greenhouse/climate_station/end'}, 'block/cube')

    #for block, tag in SIMPLE_BLOCKS.items():
    #    rm.blockstate(block).with_block_model().with_tag(tag).with_lang(lang(block)).with_item_model()
    for item in SIMPLE_ITEMS:
        rm.item_model(item).with_lang(lang(item))
    for item in SIMPLE_FOODS:
        rm.item_model('food/%s' % item).with_lang(lang(item))
    for item in SIMPLE_SPICES:
        rm.item_model('spice/%s' % item).with_lang(lang(item))
    for be in BLOCK_ENTITIES:
        rm.lang('firmalife.block_entity.%s' % be, lang(be))

    for key, value in DEFAULT_LANG.items():
        rm.lang(key, value)


def item_model_property(rm: ResourceManager, name_parts: utils.ResourceIdentifier, overrides: utils.Json, data: Dict[str, Any]) -> ItemContext:
    res = utils.resource_location(rm.domain, name_parts)
    rm.write((*rm.resource_dir, 'assets', res.domain, 'models', 'item', res.path), {
        **data,
        'overrides': overrides
    })
    return ItemContext(rm, res)


def four_rotations(model: str, rots: Tuple[Any, Any, Any, Any], suffix: str = '', prefix: str = '') -> Dict[str, Dict[str, Any]]:
    return {
        '%sfacing=east%s' % (prefix, suffix): {'model': model, 'y': rots[0]},
        '%sfacing=north%s' % (prefix, suffix): {'model': model, 'y': rots[1]},
        '%sfacing=south%s' % (prefix, suffix): {'model': model, 'y': rots[2]},
        '%sfacing=west%s' % (prefix, suffix): {'model': model, 'y': rots[3]}
    }

def four_rotations_mp_free(model: str, rots: Tuple[Any, Any, Any, Any]) -> List:
    return [
        [{'facing': 'east'}, {'model': model, 'y': rots[0]}],
        [{'facing': 'north'}, {'model': model, 'y': rots[1]}],
        [{'facing': 'south'}, {'model': model, 'y': rots[2]}],
        [{'facing': 'west'}, {'model': model, 'y': rots[3]}]
    ]

def four_rotations_mp(model: str, rots: Tuple[Any, Any, Any, Any], condition_name: str, condition_value: Any) -> List:
    return [
        [{'facing': 'east', condition_name: condition_value}, {'model': model, 'y': rots[0]}],
        [{'facing': 'north', condition_name: condition_value}, {'model': model, 'y': rots[1]}],
        [{'facing': 'south', condition_name: condition_value}, {'model': model, 'y': rots[2]}],
        [{'facing': 'west', condition_name: condition_value}, {'model': model, 'y': rots[3]}]
    ]

def greenhouse_stairs(rm: ResourceManager, name: str, frame: str, glass: str) -> 'BlockContext':
    block_name = '%s_greenhouse_roof' % name
    stair_model = 'firmalife:block/greenhouse/%s_roof' % name
    stair_model_inner = 'firmalife:block/greenhouse/%s_roof_inner' % name
    stair_model_outer = 'firmalife:block/greenhouse/%s_roof_outer' % name

    textures = {'glass': glass, 'steel': frame}
    block = rm.blockstate(block_name, variants=block_states.stairs_variants(stair_model, stair_model_inner, stair_model_outer))
    rm.block_model('greenhouse/%s_roof' % name, textures=textures, parent='firmalife:block/greenhouse_roof')
    rm.block_model('greenhouse/%s_roof_inner' % name, textures=textures, parent='firmalife:block/greenhouse_roof_inner')
    rm.block_model('greenhouse/%s_roof_outer' % name, textures=textures, parent='firmalife:block/greenhouse_roof_outer')
    rm.item_model(block_name, parent='firmalife:block/greenhouse/%s_roof' % name, no_textures=True)
    block.with_block_loot('firmalife:%s' % block_name).with_lang(lang('%s greenhouse roof', name))
    greenhouse_tags(block, name).with_tag('minecraft:stairs')
    return block

def greenhouse_slab(rm: ResourceManager, name: str, frame: str, glass: str) -> 'BlockContext':
    wall = 'firmalife:block/greenhouse/%s_wall_both' % name
    top = 'firmalife:block/greenhouse/%s_roof_top' % name
    top_upper = 'firmalife:block/greenhouse/%s_roof_top_upper' % name
    block_name = '%s_greenhouse_roof_top' % name

    textures = {'glass': glass, 'steel': frame}
    block = rm.blockstate(block_name, variants=block_states.slab_variants(wall, top, top_upper)).with_lang(lang('%s greenhouse roof top', name))
    rm.block_model('greenhouse/%s_roof_top' % name, textures, parent='firmalife:block/greenhouse_roof_top')
    rm.block_model('greenhouse/%s_roof_top_upper' % name, textures, parent='firmalife:block/greenhouse_roof_top_upper')
    rm.item_model(block_name, parent='firmalife:block/greenhouse/%s_roof_top' % name, no_textures=True)
    slab_loot(block, 'firmalife:%s' % block_name)
    greenhouse_tags(block, name).with_tag('minecraft:slabs')
    return block

def greenhouse_wall(rm: ResourceManager, name: str, frame: str, glass: str) -> 'BlockContext':
    rm.block_model('greenhouse/%s_wall' % name, {'glass': glass + '_both', 'steel': frame}, parent='firmalife:block/greenhouse_wall')
    rm.block_model('greenhouse/%s_wall_down' % name, {'glass': glass + '_down', 'steel': frame}, parent='firmalife:block/greenhouse_wall_down')
    rm.block_model('greenhouse/%s_wall_up' % name, {'glass': glass + '_up', 'steel': frame}, parent='firmalife:block/greenhouse_wall_up')
    rm.block_model('greenhouse/%s_wall_both' % name, {'glass': glass, 'steel': frame}, parent='firmalife:block/greenhouse_wall_both')

    block = rm.blockstate('%s_greenhouse_wall' % name, variants={
        'down=false,up=false': {'model': 'firmalife:block/greenhouse/%s_wall' % name},
        'down=true,up=false': {'model': 'firmalife:block/greenhouse/%s_wall_down' % name},
        'down=false,up=true': {'model': 'firmalife:block/greenhouse/%s_wall_up' % name},
        'down=true,up=true': {'model': 'firmalife:block/greenhouse/%s_wall_both' % name}
    }).with_block_loot('firmalife:%s_greenhouse_wall' % name).with_lang(lang('%s greenhouse wall', name))
    rm.item_model('%s_greenhouse_wall' % name, parent='firmalife:block/greenhouse/%s_wall_both' % name, no_textures=True)
    greenhouse_tags(block, name)
    return block

def greenhouse_door(rm: ResourceManager, name: str, bot: str, upper: str) -> 'BlockContext':
    door = '%s_greenhouse_door' % name
    door_model = 'greenhouse/%s_door' % name
    block = 'firmalife:block/greenhouse/%s_door' % name
    bottom = block + '_bottom'
    bottom_hinge = block + '_bottom_hinge'
    top = block + '_top'
    top_hinge = block + '_top_hinge'

    block = rm.blockstate(door, variants=block_states.door_blockstate(bottom, bottom_hinge, top, top_hinge)).with_lang(lang('%s greenhouse door', name))
    rm.block_model(door_model + '_bottom', {'bottom': bot}, parent='block/door_bottom')
    rm.block_model(door_model + '_bottom_hinge', {'bottom': bot}, parent='block/door_bottom_rh')
    rm.block_model(door_model + '_top', {'top': upper}, parent='block/door_top')
    rm.block_model(door_model + '_top_hinge', {'top': upper}, parent='block/door_top_rh')
    rm.item_model(door)
    door_loot(block, 'firmalife:%s' % door)
    greenhouse_tags(block, name).with_tag('minecraft:doors')
    return block

def greenhouse_tags(block: BlockContext, greenhouse_name: str) -> 'BlockContext':
    block.with_tag('%s_greenhouse' % greenhouse_name)
    if greenhouse_name in ('weathered_treated_wood', 'treated_wood'):
        block.with_tag('minecraft:mineable/axe')
    else:
        block.with_tag('minecraft:mineable/pickaxe')
    return block

def slab_loot(block: BlockContext, loot: str) -> 'BlockContext':
    return block.with_block_loot({
        'name': loot,
        'functions': [{
            'function': 'minecraft:set_count',
            'conditions': [loot_tables.block_state_property(loot + '[type=double]')],
            'count': 2,
            'add': False
        }]
    })

def door_loot(block: BlockContext, loot: str) -> 'BlockContext':
    return block.with_block_loot({'name': loot, 'conditions': [loot_tables.block_state_property(loot + '[half=lower]')]})
