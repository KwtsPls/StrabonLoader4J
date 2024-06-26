ALTER TABLE triples ADD CONSTRAINT triples_pkey PRIMARY KEY          (pred, obj, subj, ctx, expl);
CREATE INDEX triples_pred_subj_obj_idx      ON triples  USING btree  (pred, subj, obj);
--CREATE INDEX triples_subj_pred_obj_idx      ON triples  USING btree  (subj, pred, obj);
CREATE INDEX triples_subj_obj_pred_idx      ON triples  USING btree  (subj, obj, pred);
--CREATE INDEX triples_obj_pred_subj_idx      ON triples  USING btree  (obj, pred, subj);
CREATE INDEX triples_obj_subj_pred_idx      ON triples  USING btree  (obj, subj, pred);
CREATE INDEX triples_ctx_pred_obj_subj_idx  ON triples  USING btree  (ctx, pred, obj, subj);
CREATE INDEX triples_ctx_pred_subj_obj_idx  ON triples  USING btree  (ctx, pred, subj, obj);
CREATE INDEX triples_ctx_obj_subj_idx       ON triples  USING btree  (ctx, obj, subj);
--CREATE INDEX triples_ctx_subj_objidx        ON triples  USING btree  (ctx, subj, obj);
