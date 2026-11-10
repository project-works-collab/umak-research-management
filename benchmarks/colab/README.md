# Colab synthetic duplicate benchmark

1. Open https://colab.research.google.com/ and choose File → Upload notebook.
2. Upload `duplicate_benchmark_colab.ipynb`.
3. Run all cells; when prompted upload `records.csv`, `queries.csv`, and `labels.csv` together.
4. Download the results ZIP from the last cell.

Includes 120 records, 480 query submissions and 57,600 query–record labels. Half of source studies are for development, half for testing. Synthetic labels are generated from source identity, not real reviewer annotations. No GitHub account needed. First run downloads packages and a pretrained model. Use a fresh CPU runtime. GPU is not used by this benchmark. The notebook has been structurally and syntactically checked locally; full execution requires Colab and internet access.
