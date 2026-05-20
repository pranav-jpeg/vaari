from flask import Flask, jsonify, request
import csv

app = Flask(__name__)

# Aliases map — common names to CSV crop names
ALIASES = {
    'corn': 'maize (corn)',
    'maize': 'maize (corn)',
    'coffee': 'coffee, green',
    'soybean': 'soya beans',
    'soybeans': 'soya beans',
    'soy': 'soya beans',
    'cotton': 'seed cotton, unginned',
    'groundnut': 'groundnuts, excluding shelled',
    'groundnuts': 'groundnuts, excluding shelled',
    'peanut': 'groundnuts, excluding shelled',
    'peanuts': 'groundnuts, excluding shelled',
    'chickpea': 'chick peas, dry',
    'chickpeas': 'chick peas, dry',
    'chana': 'chick peas, dry',
    'lentil': 'lentils, dry',
    'lentils': 'lentils, dry',
    'masoor': 'lentils, dry',
    'pepper': 'chillies and peppers, green (capsicum spp. and pimenta spp.)',
    'chilli': 'chillies and peppers, green (capsicum spp. and pimenta spp.)',
    'mango': 'mangoes, guavas and mangosteens',
    'mangoes': 'mangoes, guavas and mangosteens',
    'orange': 'oranges',
    'sugarcane': 'sugar cane',
    'sugar cane': 'sugar cane',
    'mustard': 'rape or colza seed',
    'canola': 'rape or colza seed',
    'sunflower': 'sunflower seed',
    'coconut': 'coconuts, in shell',
    'almond': 'almonds, in shell',
    'almonds': 'almonds, in shell',
    'cashew': 'cashew nuts, in shell',
    'cashews': 'cashew nuts, in shell',
    'walnut': 'walnuts, in shell',
    'walnuts': 'walnuts, in shell',
    'banana': 'bananas',
    'grape': 'grapes',
    'apple': 'apples',
    'onion': 'onions and shallots, dry (excluding dehydrated)',
    'onions': 'onions and shallots, dry (excluding dehydrated)',
    'pyaz': 'onions and shallots, dry (excluding dehydrated)',
    'garlic': 'green garlic',
    'tomato': 'tomatoes',
    'potato': 'potatoes',
    'aloo': 'potatoes',
    'jowar': 'sorghum',
    'bajra': 'millet',
    'arhar': 'cow peas, dry',
    'tur': 'cow peas, dry',
    'moong': 'beans, dry',
    'urad': 'beans, dry',
    'flaxseed': 'linseed',
    'sesame': 'sesame seed',
    'til': 'sesame seed',
    'ginger': 'ginger, raw',
    'adrak': 'ginger, raw',
    'tea': 'tea leaves',
    'cocoa': 'cocoa beans',
    'chocolate': 'cocoa beans',
    'olive': 'olives',
    'olives': 'olives',
    'avocado': 'avocados',
    'lettuce': 'lettuce and chicory',
    'carrot': 'carrots and turnips',
    'carrots': 'carrots and turnips',
    'asparagus': 'asparagus',
    'eggplant': 'eggplants (aubergines)',
    'brinjal': 'eggplants (aubergines)',
    'baingan': 'eggplants (aubergines)',
    'cauliflower': 'cauliflowers and broccoli',
    'broccoli': 'cauliflowers and broccoli',
    'cabbage': 'cabbages',
    'pumpkin': 'pumpkins, squash and gourds',
    'squash': 'pumpkins, squash and gourds',
    'cucumber': 'cucumbers and gherkins',
    'melon': 'cantaloupes and other melons',
    'watermelon': 'watermelons',
    'strawberry': 'strawberries',
    'strawberries': 'strawberries',
    'plum': 'plums and sloes',
    'peach': 'peaches and nectarines',
    'pear': 'pears',
    'cherry': 'cherries',
    'fig': 'figs',
    'date': 'dates',
    'hemp': 'hempseed',
    'oat': 'oats',
    'oats': 'oats',
    'rye': 'rye',
    'barley': 'barley',
    'sorghum': 'sorghum',
    'millet': 'millet',
    'cassava': 'cassava, fresh',
    'tapioca': 'cassava, fresh',
    'yam': 'yams',
    'sweet potato': 'sweet potatoes',
    'shakarkand': 'sweet potatoes',
}

def find_crop(query):
    query = query.strip().lower()

    # 1. Check aliases first
    if query in ALIASES:
        query = ALIASES[query]

    # 2. Exact match
    for crop in crops:
        if crop['crop_name'].lower() == query:
            return crop

    # 3. Partial match (query inside crop name)
    for crop in crops:
        if query in crop['crop_name'].lower():
            return crop

    # 4. Reverse partial match (crop name inside query)
    for crop in crops:
        if crop['crop_name'].lower() in query:
            return crop

    return None

crops = []

with open('vaari_crops.csv', newline='', encoding='utf-8') as f:
    reader = csv.DictReader(f)
    for row in reader:
        crops.append(row)

def find_crop(query):
    query = query.strip().lower()
    for crop in crops:
        if crop['crop_name'].lower() == query:
            return crop
    for crop in crops:
        if query in crop['crop_name'].lower():
            return crop
    return None

def build_response(crop):
    green = float(crop['green_wf'])
    blue = float(crop['blue_wf'])
    grey = float(crop['grey_wf'])
    total = float(crop['total_wf'])
    return jsonify({
        "productName": crop['crop_name'],
        "totalWater": round(total, 1),
        "greenWater": round(green, 1),
        "blueWater": round(blue, 1),
        "greyWater": round(grey, 1),
        "waterSavingTip": "Use drip irrigation to save water",
        "irrigationType": "Mixed",
        "climate": "Varies",
        "waterScarcity": "Moderate",
        "harvestSeason": "Varies",
        "unit": "L/kg"
    })

@app.route('/crop', methods=['GET'])
def get_crop():
    name = request.args.get('name', '').strip()
    if not name:
        return jsonify({"error": "Please provide a crop name"}), 400
    crop = find_crop(name)
    if not crop:
        return jsonify({"error": f"Crop '{name}' not found. Try another name."}), 404
    return build_response(crop)

@app.route('/api/search', methods=['GET'])
def search_crop():
    query = request.args.get('query', '').strip()
    if not query:
        return jsonify({"error": "Please provide a crop name"}), 400
    crop = find_crop(query)
    if not crop:
        return jsonify({"error": f"Crop '{query}' not found. Try another name."}), 404
    return build_response(crop)

@app.route('/suggest', methods=['GET'])
def suggest():
    q = request.args.get('q', '').strip().lower()
    if not q:
        return jsonify([])

    results = [
        crop['crop_name']
        for crop in crops
        if crop['crop_name'].lower().startswith(q)
    ]

    return jsonify(results[:10])

@app.route('/debug')
def debug():
    if crops:
        return jsonify({
            "total_crops": len(crops),
            "first_row_keys": list(crops[0].keys()),
            "first_row_sample": dict(crops[0])
        })
    return jsonify({"error": "crops list is empty"})

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)