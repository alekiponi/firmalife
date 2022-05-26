from mcresources import ResourceManager, RecipeContext, utils
from mcresources.type_definitions import Json

from constants import *


def generate(rm: ResourceManager):
    # Crafting
    rm.crafting_shaped('crafting/peel', ['X', 'Y'], {'X': 'minecraft:bowl', 'Y': '#forge:rods/wooden'}, 'firmalife:peel').with_advancement('#forge:rods/wooden')

    # Firmalife
    drying_recipe(rm, 'drying_fruit', intersect(utils.ingredient('#tfc:foods/fruits'), not_ingredient(has_trait(None, 'firmalife:dried'))), item_stack_provider(copy_input=True, add_trait='firmalife:dried'))
    drying_recipe(rm, 'cinnamon', 'firmalife:cinnamon_bark', item_stack_provider('firmalife:spice/cinnamon'))

    # TFC
    for block in GREENHOUSE_BLOCKS:
        if block != 'door':
            for first, second in CLEANING_PAIRS.items():
                chisel_recipe(rm, 'cleaning/%s_greenhouse_%s' % (first, block), 'firmalife:%s_greenhouse_%s' % (first, block), 'firmalife:%s_greenhouse_%s' % (second, block), 'smooth')


def chisel_recipe(rm: ResourceManager, name_parts: utils.ResourceIdentifier, ingredient: utils.Json, result: str, mode: str):
    rm.recipe(('chisel', mode, name_parts), 'tfc:chisel', {
        'ingredient': ingredient,
        'result': result,
        'mode': mode,
        'extra_drop': item_stack_provider(result) if mode == 'slab' else None
    })

def drying_recipe(rm: ResourceManager, name: utils.ResourceIdentifier, item: Any, result: Json) -> RecipeContext:
    return rm.recipe(('drying', name), 'firmalife:drying', {
        'ingredient': utils.ingredient(item) if isinstance(item, str) else item,
        'result': result
    })

def intersect(ingredient1: Json, ingredient2: Json):
    return {
        'type': 'forge:intersection',
        'children': [ingredient1, ingredient2]
    }

def has_trait(ingredient: Json, trait: str) -> Json:
    return {
        'type': 'tfc:has_trait',
        'trait': trait,
        'ingredient': utils.ingredient(ingredient) if ingredient is not None else None
    }

def not_ingredient(data_in: Json):
    return {'type': 'tfc:not', 'ingredient': data_in}

def item_stack_provider(data_in: Json = None, copy_input: bool = False, copy_heat: bool = False, copy_food: bool = False, reset_food: bool = False, add_heat: float = None, add_trait: str = None, remove_trait: str = None, empty_bowl: bool = False) -> Json:
    if isinstance(data_in, dict):
        return data_in
    stack = utils.item_stack(data_in) if data_in is not None else None
    modifiers = [k for k, v in (
        ('tfc:copy_input', copy_input),
        ('tfc:copy_heat', copy_heat),
        ('tfc:copy_food', copy_food),
        ('tfc:reset_food', reset_food),
        ('tfc:empty_bowl', empty_bowl),
        ({'type': 'tfc:add_heat', 'temperature': add_heat}, add_heat is not None),
        ({'type': 'tfc:add_trait', 'trait': add_trait}, add_trait is not None),
        ({'type': 'tfc:remove_trait', 'trait': remove_trait}, remove_trait is not None)
    ) if v]
    if modifiers:
        return {
            'stack': stack,
            'modifiers': modifiers
        }
    return stack

