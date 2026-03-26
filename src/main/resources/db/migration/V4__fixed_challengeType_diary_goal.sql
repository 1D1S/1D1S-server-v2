UPDATE diary_goal dg

    JOIN diary d
    ON d.id = dg.diary_id

    JOIN challenge c
    ON c.id = d.challenge_id

    JOIN challenge_goal cur_g
    ON cur_g.id = dg.challenge_goal_id

    JOIN participant cur_p
    ON cur_p.id = cur_g.participant_id
    AND cur_p.challenge_id = d.challenge_id
    AND cur_p.member_id = d.member_id

    JOIN participant host_p
    ON host_p.challenge_id = c.id
    AND host_p.member_id = c.host_member_id
    AND host_p.status = 'HOST'

    JOIN challenge_goal host_g
    ON host_g.participant_id = host_p.id
    AND host_g.content = cur_g.content

SET dg.challenge_goal_id = host_g.id

WHERE c.type = 'FIXED'
  AND c.start_date BETWEEN '2026-03-16' AND '2026-03-21'
  AND d.created_at >= '2026-03-16 00:00:00'
  AND d.created_at <  '2026-03-21 00:00:00'
  AND dg.challenge_goal_id <> host_g.id;