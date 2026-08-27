import unittest

from app.flower_catalog import (
    get_flower_by_model_label,
    get_flower_id_for_model_label,
)


class FlowerCatalogTest(unittest.TestCase):
    def test_model_labels_map_to_expected_flower_ids(self):
        expected_ids = {
            "daisy": "papatya",
            "dandelion": "karahindiba",
            "roses": "gul",
            "sunflowers": "aycicegi",
            "tulips": "lale",
        }

        for model_label, flower_id in expected_ids.items():
            with self.subTest(model_label=model_label):
                self.assertEqual(
                    flower_id,
                    get_flower_id_for_model_label(model_label),
                )

    def test_catalog_returns_turkish_display_names(self):
        expected_names = {
            "daisy": "Papatya",
            "dandelion": "Karahindiba",
            "roses": "Gül",
            "sunflowers": "Ayçiçeği",
            "tulips": "Lale",
        }

        for model_label, display_name in expected_names.items():
            with self.subTest(model_label=model_label):
                flower = get_flower_by_model_label(model_label)

                self.assertIsNotNone(flower)
                self.assertEqual(display_name, flower.commonName)

    def test_unknown_model_label_returns_none(self):
        self.assertIsNone(get_flower_id_for_model_label("unknown"))
        self.assertIsNone(get_flower_by_model_label("unknown"))


if __name__ == "__main__":
    unittest.main()
